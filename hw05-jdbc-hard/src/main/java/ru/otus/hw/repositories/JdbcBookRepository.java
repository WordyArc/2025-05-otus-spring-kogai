package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JdbcBookRepository implements BookRepository {

    private final NamedParameterJdbcOperations jdbc;

    private final GenreRepository genreRepository;

    @Override
    public Optional<Book> findById(long id) {
        var sql = """
                SELECT b.id AS b_id,
                       b.title,
                       a.id AS a_id,
                       a.full_name AS a_full_name,
                       g.id AS g_id,
                       g.name AS g_name
                FROM books b
                JOIN authors a ON a.id = b.author_id
                LEFT JOIN books_genres bg ON bg.book_id = b.id
                LEFT JOIN genres g ON g.id = bg.genre_id
                WHERE b.id = :id
                ORDER BY g.id
                """;
        var book = jdbc.query(sql, Map.of("id", id), new BookResultSetExtractor());
        return Optional.ofNullable(book);
    }

    @Override
    public List<Book> findAll() {
        var genres = genreRepository.findAll();
        var books = getAllBooksWithoutGenres();
        var relations = getAllGenreRelations();
        mergeBooksInfo(books, genres, relations);
        return books;
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            return insert(book);
        }
        return update(book);
    }

    @Override
    public void deleteById(long id) {
        jdbc.update("DELETE FROM books WHERE id = :id", Map.of("id", id));
    }

    private List<Book> getAllBooksWithoutGenres() {
        var sql = """
                SELECT b.id,
                       b.title,
                       a.id AS a_id,
                       a.full_name AS a_full_name
                FROM books b
                JOIN authors a ON a.id = b.author_id
                ORDER BY b.id
                """;
        return jdbc.query(sql, new BookRowMapper());
    }

    private List<BookGenreRelation> getAllGenreRelations() {
        var sql = """
                SELECT book_id, genre_id
                FROM books_genres
                ORDER BY book_id, genre_id
                """;
        return jdbc.query(sql, (rs, rn) ->
                new BookGenreRelation(rs.getLong("book_id"), rs.getLong("genre_id")));
    }

    private void mergeBooksInfo(List<Book> booksWithoutGenres,
                                List<Genre> genres,
                                List<BookGenreRelation> relations) {
        if (booksWithoutGenres.isEmpty() || relations.isEmpty()) {
            return;
        }
        final Map<Long, Book> booksById = booksWithoutGenres.stream()
                .collect(Collectors.toMap(Book::getId, b -> b));
        final Map<Long, Genre> genresById = genres.stream()
                .collect(Collectors.toMap(Genre::getId, g -> g));

        for (var rel : relations) {
            var book = booksById.get(rel.bookId());
            var genre = genresById.get(rel.genreId());
            if (book != null && genre != null) {
                if (book.getGenres() == null) {
                    book.setGenres(new ArrayList<>());
                }
                book.getGenres().add(genre);
            }
        }
    }

    private Book insert(Book book) {
        var keyHolder = new GeneratedKeyHolder();
        var params = new MapSqlParameterSource()
                .addValue("title", book.getTitle())
                .addValue("authorId", book.getAuthor().getId());

        jdbc.update("""
                INSERT INTO books(title, author_id)
                VALUES (:title, :authorId)
                """, params, keyHolder, new String[]{"id"});

        //noinspection DataFlowIssue
        book.setId(keyHolder.getKeyAs(Long.class));
        batchInsertGenresRelationsFor(book);
        return book;
    }

    private Book update(Book book) {
        var updated = jdbc.update("""
                        UPDATE books
                        SET title = :title,
                            author_id = :authorId
                        WHERE id = :id
                        """,
                new MapSqlParameterSource()
                        .addValue("id", book.getId())
                        .addValue("title", book.getTitle())
                        .addValue("authorId", book.getAuthor().getId()));

        if (updated == 0) {
            throw new EntityNotFoundException("Book with id %d not found".formatted(book.getId()));
        }

        removeGenresRelationsFor(book);
        batchInsertGenresRelationsFor(book);
        return book;
    }

    private void batchInsertGenresRelationsFor(Book book) {
        var genres = book.getGenres();
        if (genres == null || genres.isEmpty()) {
            return;
        }
        var batch = genres.stream()
                .map(g -> new MapSqlParameterSource()
                        .addValue("bookId", book.getId())
                        .addValue("genreId", g.getId()))
                .toArray(MapSqlParameterSource[]::new);

        jdbc.batchUpdate("""
                INSERT INTO books_genres(book_id, genre_id)
                VALUES (:bookId, :genreId)
                """, batch);
    }

    private void removeGenresRelationsFor(Book book) {
        jdbc.update("""
                DELETE FROM books_genres
                WHERE book_id = :bookId
                """, Map.of("bookId", book.getId()));
    }

    private static class BookRowMapper implements RowMapper<Book> {
        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            var author = new Author(rs.getLong("a_id"), rs.getString("a_full_name"));
            return new Book(
                    rs.getLong("id"),
                    rs.getString("title"),
                    author,
                    new ArrayList<>()
            );
        }
    }

    @RequiredArgsConstructor
    private static class BookResultSetExtractor implements ResultSetExtractor<Book> {
        @Override
        public Book extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (!rs.next()) {
                return null;
            }
            var bookId = rs.getLong("b_id");
            var title = rs.getString("title");
            final var author = new Author(rs.getLong("a_id"), rs.getString("a_full_name"));
            final var genres = new ArrayList<Genre>();

            do {
                var genreId = rs.getLong("g_id");
                if (!rs.wasNull()) {
                    genres.add(new Genre(genreId, rs.getString("g_name")));
                }
            } while (rs.next());

            return new Book(bookId, title, author, genres);
        }
    }

    private record BookGenreRelation(long bookId, long genreId) {
    }
}
