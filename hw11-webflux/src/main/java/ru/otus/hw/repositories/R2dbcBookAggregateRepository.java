package ru.otus.hw.repositories;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

@Repository
@RequiredArgsConstructor
public class R2dbcBookAggregateRepository implements BookAggregateRepository {

    private final R2dbcEntityOperations ops;

    private final ObjectMapper objectMapper;

    private static final String BASE_JSON = """
            select
              b.id  as id,
              b.title   as title,
              json_object('id', a.id, 'fullName', a.full_name) as author,
              json_array(
                select json_object('id', g.id, 'name', g.name)
                from books_genres bg
                join genres g on g.id = bg.genre_id
                where bg.book_id = b.id
                order by g.id
              ) as genres
            from books b
            join authors a on a.id = b.author_id
            
            """;

    @Override
    public Flux<Book> findAllAggregates() {
        return ops.getDatabaseClient().sql(BASE_JSON + "order by b.id")
                .map(bookAggregateMapper)
                .all();
    }

    @Override
    public Mono<Book> findAggregateById(Long id) {
        return ops.getDatabaseClient()
                .sql(BASE_JSON + "where b.id = :id")
                .bind("id", id)
                .map(bookAggregateMapper)
                .one();
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

    private final BiFunction<Row, RowMetadata, Book> bookAggregateMapper = (row, md) -> mapRow(
            row.get("id", Long.class),
            row.get("title", String.class),
            row.get("author", String.class),
            row.get("genres", String.class)
    );

    private Book mapRow(Long id, String title, String authorJson, String genresJson) {
        try {
            var author = objectMapper.readValue(authorJson, Author.class);

            List<Genre> genres = (genresJson == null || genresJson.isBlank())
                    ? Collections.emptyList()
                    : objectMapper.readValue(genresJson, new TypeReference<>() {
            });

            return new Book(id, title, author.getId(), author, genres);
        } catch (Exception e) {
            throw new DataRetrievalFailureException("Failed to parse aggregated JSON for book id=" + id, e);
        }
    }

}
