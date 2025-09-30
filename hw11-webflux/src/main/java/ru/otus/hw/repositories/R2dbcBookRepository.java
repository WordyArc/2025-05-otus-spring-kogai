package ru.otus.hw.repositories;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


@Repository
@RequiredArgsConstructor
public class R2dbcBookRepository implements BookAggregateRepository {

    private final R2dbcEntityOperations ops;

    private static final String BASE_JOIN = """
            select
                b.id        as b_id,
                b.title     as b_title,
                a.id        as a_id,
                a.full_name as a_name,
                g.id        as g_id,
                g.name      as g_name
            from books b
            join authors a on a.id = b.author_id
            left join books_genres bg on bg.book_id = b.id
            left join genres g on g.id = bg.genre_id
            """;

    @Override
    public Flux<Book> findAllAggregates() {
        return ops.getDatabaseClient()
                .sql(BASE_JOIN + " order by b.id, g.id")
                .map(this::mapJoin).all()
                .bufferUntilChanged(BookJoin::bookId)
                .map(this::collapse);
    }

    @Override
    public Mono<Book> findAggregateById(Long id) {
        return ops.getDatabaseClient()
                .sql(BASE_JOIN + " where b.id = :id order by g.id")
                .bind("id", id)
                .map(this::mapJoin).all()
                .collectList()
                .filter(list -> !list.isEmpty())
                .map(this::collapse);
    }

    @Override
    public Mono<Void> replaceGenres(Long bookId, Iterable<Long> genreIds) {
        var delete = ops.getDatabaseClient()
                .sql("delete from books_genres where book_id = :bid")
                .bind("bid", bookId)
                .fetch().rowsUpdated();

        var batchInsert = Flux.fromIterable(genreIds).concatMap(gid ->
                ops.getDatabaseClient()
                        .sql("insert into books_genres(book_id, genre_id) values(:bid, :gid)")
                        .bind("bid", bookId)
                        .bind("gid", gid)
                        .fetch().rowsUpdated());

        return delete.thenMany(batchInsert).then();
    }

    private Book collapse(List<BookJoin> rows) {
        var head = rows.get(0);

        var book = new Book();
        book.setId(head.bookId());
        book.setTitle(head.bookTitle());
        book.setAuthorId(head.authorId());
        book.setAuthor(new Author(head.authorId(), head.authorName()));

        final var genres = new LinkedHashMap<Long, Genre>();
        for (var r : rows) {
            var gid = r.genreId();
            if (gid == null) continue;
            genres.computeIfAbsent(gid, id -> new Genre(id, r.genreName()));
        }
        book.setGenres(new ArrayList<>(genres.values()));

        return book;
    }

    private BookJoin mapJoin(Row row, RowMetadata md) {
        return new BookJoin(
                toLong(row.get("b_id")), (String) row.get("b_title"),
                toLong(row.get("a_id")), (String) row.get("a_name"),
                toLong(row.get("g_id")), (String) row.get("g_name")
        );
    }


    private record BookJoin(
            Long bookId, String bookTitle,
            Long authorId, String authorName,
            Long genreId, String genreName
    ) {
    }

    private static Long toLong(Object o) {
        return (o == null) ? null : ((Number) o).longValue();
    }
}
