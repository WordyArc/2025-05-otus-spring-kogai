package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Genre;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(JdbcGenreRepository.class)
class JdbcGenreRepositoryTest {

    @Autowired
    private JdbcGenreRepository repository;

    @Test
    @DisplayName("should load all genres")
    void shouldFindAll() {
        var genres = repository.findAll();
        assertThat(genres).containsExactly(
                new Genre(1, "Genre_1"),
                new Genre(2, "Genre_2"),
                new Genre(3, "Genre_3"),
                new Genre(4, "Genre_4"),
                new Genre(5, "Genre_5"),
                new Genre(6, "Genre_6")
        );
    }

    @Nested
    @DisplayName("findAllByIds")
    class FindAllByIds {

        @Test
        @DisplayName("should load genres by ids preserving ascending order")
        void shouldFindByIds() {
            var genres = repository.findAllByIds(Set.of(5L, 2L));
            assertThat(genres).containsExactly(
                    new Genre(2, "Genre_2"),
                    new Genre(5, "Genre_5")
            );
        }

        @Test
        @DisplayName("should return empty list when ids is empty")
        void shouldReturnEmptyOnEmptyIds() {
            var genres = repository.findAllByIds(Set.of());
            assertThat(genres).isEmpty();
        }
    }
}
