package ru.otus.hw.persistence.mongo.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import ru.otus.hw.persistence.mongo.model.GenreDocument;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class MongoGenreRepositoryTest {

    @Autowired
    private MongoGenreRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        repository.saveAll(List.of(
                new GenreDocument(null, "Genre_1"),
                new GenreDocument(null, "Genre_2"),
                new GenreDocument(null, "Genre_3"),
                new GenreDocument(null, "Genre_4"),
                new GenreDocument(null, "Genre_5"),
                new GenreDocument(null, "Genre_6")
        ));
    }

    @Test
    @DisplayName("should load all genres")
    void shouldFindAll() {
        var list = repository.findAll();
        assertThat(list).hasSize(6);
        assertThat(list).extracting(GenreDocument::getName)
                .containsExactlyInAnyOrder("Genre_1", "Genre_2", "Genre_3", "Genre_4", "Genre_5", "Genre_6");
    }

    @Nested
    @DisplayName("findAllByIds")
    class FindAllByIds {
        @Test
        @DisplayName("should return exactly requested ids")
        void ordered() {
            var savedGenres = repository.findAll();
            var genre2Id = savedGenres.stream()
                    .filter(g -> "Genre_2".equals(g.getName()))
                    .findFirst()
                    .orElseThrow()
                    .getId();
            var genre5Id = savedGenres.stream()
                    .filter(g -> "Genre_5".equals(g.getName()))
                    .findFirst()
                    .orElseThrow()
                    .getId();
            
            var list = repository.findAllByIdIn(Set.of(genre5Id, genre2Id));
            assertThat(list).hasSize(2);
            assertThat(list).extracting(GenreDocument::getName)
                    .containsExactlyInAnyOrder("Genre_2", "Genre_5");
        }

        @Test
        @DisplayName("should return empty for empty ids")
        void empty() {
            assertThat(repository.findAllByIdIn(Collections.emptySet())).isEmpty();
        }
    }
}
