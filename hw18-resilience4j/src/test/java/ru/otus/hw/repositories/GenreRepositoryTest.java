package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.models.Genre;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class GenreRepositoryTest {

    @Autowired
    private GenreRepository repository;

    @Autowired
    private TestEntityManager em;

    private Genre genre1;
    private Genre genre2;
    private Genre genre3;

    @BeforeEach
    void setUp() {
        genre1 = new Genre(null, "Fantasy");
        genre2 = new Genre(null, "Science Fiction");
        genre3 = new Genre(null, "Mystery");

        genre1 = em.persistAndFlush(genre1);
        genre2 = em.persistAndFlush(genre2);
        genre3 = em.persistAndFlush(genre3);
    }

    @Test
    @DisplayName("should load all genres")
    void shouldFindAll() {
        var list = repository.findAll();
        assertThat(list).hasSize(3);
        assertThat(list).extracting(Genre::getId)
                .containsExactlyInAnyOrder(genre1.getId(), genre2.getId(), genre3.getId());
        assertThat(list).extracting(Genre::getName)
                .containsExactlyInAnyOrder("Fantasy", "Science Fiction", "Mystery");
    }

    @Nested
    @DisplayName("findAllByIds")
    class FindAllByIds {
        @Test
        @DisplayName("should return exactly requested ids")
        void ordered() {
            var list = repository.findAllByIdIn(Set.of(genre1.getId(), genre3.getId()));
            assertThat(list).hasSize(2);
            assertThat(list).extracting(Genre::getId)
                    .containsExactlyInAnyOrder(genre1.getId(), genre3.getId());
        }

        @Test
        @DisplayName("should return empty for empty ids")
        void empty() {
            assertThat(repository.findAllByIdIn(Set.of())).isEmpty();
        }
    }
}
