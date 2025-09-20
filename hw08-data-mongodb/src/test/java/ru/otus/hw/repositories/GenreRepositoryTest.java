package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import ru.otus.hw.MongoIntegrationTest;
import ru.otus.hw.models.Genre;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class GenreRepositoryTest extends MongoIntegrationTest {

    @Autowired
    private GenreRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        repository.saveAll(List.of(
                new Genre("g1", "Genre_1"),
                new Genre("g2", "Genre_2"),
                new Genre("g3", "Genre_3"),
                new Genre("g4", "Genre_4"),
                new Genre("g5", "Genre_5"),
                new Genre("g6", "Genre_6")
        ));
    }

    @Test
    @DisplayName("should load all genres")
    void shouldFindAll() {
        var list = repository.findAll();
        assertThat(list).extracting(Genre::getId)
                .containsExactlyInAnyOrder("g1", "g2", "g3", "g4", "g5", "g6");
        assertThat(list).extracting(Genre::getName)
                .containsExactlyInAnyOrder("Genre_1", "Genre_2", "Genre_3", "Genre_4", "Genre_5", "Genre_6");
    }

    @Nested
    @DisplayName("findAllByIds")
    class FindAllByIds {
        @Test
        @DisplayName("should return exactly requested ids")
        void ordered() {
            var list = repository.findAllByIdIn(Set.of("g5", "g2"));
            assertThat(list).extracting(Genre::getId)
                    .containsExactlyInAnyOrder("g2", "g5");
        }

        @Test
        @DisplayName("should return empty for empty ids")
        void empty() {
            assertThat(repository.findAllByIdIn(Collections.emptySet())).isEmpty();
        }
    }
}
