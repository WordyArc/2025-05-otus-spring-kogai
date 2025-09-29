package ru.otus.hw.repositories;

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
import java.util.Map;
import java.util.stream.Collectors;


@Repository
@RequiredArgsConstructor
public class BookRepositoryCustomImpl implements BookRepositoryCustom {

    private final R2dbcEntityOperations ops;

    private static final String BASE_JOIN = """
            select
                b.id        as b_id,
                b.title     as b_title,
                b.author_id as a_id,
                a.full_name as a_name,
                g.id        as g_id,
                g.name      as g_name
            from books b
            join authors a on a.id = b.author_id
            left join books_genres bg on bg.book_id = b.id
            left join genres g on g.id = bg.genre_id
            """;

    public Flux<Book> findAllAggregates() {
        return ops.getDatabaseClient().sql(BASE_JOIN + " order by b.id")
                .map((row, md) -> new Row(
                        getL(row.get("b_id")), (String) row.get("b_title"),
                        getL(row.get("a_id")), (String) row.get("a_name"),
                        getL(row.get("g_id")), (String) row.get("g_name")
                )).all()
                .transform(this::rowsToBooks);
    }

    public Mono<Book> findAggregateById(Long id) {
        return ops.getDatabaseClient().sql(BASE_JOIN + " where b.id=:id")
                .bind("id", id)
                .map((row, md) -> new Row(
                        getL(row.get("b_id")), (String) row.get("b_title"),
                        getL(row.get("a_id")), (String) row.get("a_name"),
                        getL(row.get("g_id")), (String) row.get("g_name")
                )).all()
                .transform(this::rowsToBooks)
                .singleOrEmpty();
    }

    public Mono<Void> replaceGenres(Long bookId, Iterable<Long> genreIds) {
        var delete = ops.getDatabaseClient().sql("delete from books_genres where book_id=:bid")
                .bind("bid", bookId).fetch().rowsUpdated();
        var inserts = Flux.fromIterable(genreIds)
                .concatMap(gid -> ops.getDatabaseClient()
                        .sql("insert into books_genres(book_id, genre_id) values(:bid,:gid)")
                        .bind("bid", bookId).bind("gid", gid)
                        .fetch().rowsUpdated());
        return delete.thenMany(inserts).then();
    }

    /* — helpers — */

    private Flux<Book> rowsToBooks(Flux<Row> rows) {
        return rows.collectList().flatMapMany(list -> {
            Map<Long, Book> acc = new LinkedHashMap<>();
            for (var r : list) {
                var b = acc.computeIfAbsent(r.bId, k -> {
                    var book = new Book();
                    book.setId(r.bId);
                    book.setTitle(r.bTitle);
                    book.setAuthorId(r.aId);
                    book.setAuthor(new Author(r.aId, r.aName));
                    book.setGenres(new ArrayList<>());
                    return book;
                });
                if (r.gId != null) b.getGenres().add(new Genre(r.gId, r.gName));
            }
            return Flux.fromIterable(acc.values()).map(b -> {
                if (!b.getGenres().isEmpty()) {
                    b.setGenres(b.getGenres().stream()
                            .collect(Collectors.collectingAndThen(
                                    Collectors.toMap(Genre::getId, g -> g, (x, y) -> x, LinkedHashMap::new),
                                    m -> new ArrayList<>(m.values()))));
                }
                return b;
            });
        });
    }

    private record Row(Long bId, String bTitle, Long aId, String aName, Long gId, String gName) {
    }

    private static Long getL(Object o) {
        return o == null ? null : ((Number) o).longValue();
    }
}
