package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Author;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcAuthorRepository implements AuthorRepository {

    private final NamedParameterJdbcOperations jdbc;

    @Override
    public List<Author> findAll() {
        return jdbc.query("SELECT id, full_name FROM authors ORDER BY id", new AuthorRowMapper());
    }

    @Override
    public Optional<Author> findById(long id) {
        List<Author> authors = jdbc.query("SELECT id, full_name FROM authors WHERE id = :id",
                Map.of("id", id),
                new AuthorRowMapper());
        return authors.stream().findFirst();
    }

    private static class AuthorRowMapper implements RowMapper<Author> {

        @Override
        public Author mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            return new Author(resultSet.getLong("id"), resultSet.getString("full_name"));
        }
    }
}
