package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.otus.hw.models.Genre;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class GenreRepositoryTest {

    @Autowired
    private GenreRepository repository;

    @Test
    @DisplayName("should load all genres")
    void shouldFindAll() {
        var list = repository.findAll();
        assertThat(list).extracting(Genre::getId)
                .containsExactlyInAnyOrder(1L, 2L, 3L, 4L, 5L, 6L);
        assertThat(list).extracting(Genre::getName)
                .containsExactlyInAnyOrder("Genre_1","Genre_2","Genre_3","Genre_4","Genre_5","Genre_6");
    }

    @Nested
    @DisplayName("findAllByIds")
    class FindAllByIds {
        @Test
        @DisplayName("should keep ascending order")
        void ordered() {
            var list = repository.findAllByIdIn(Set.of(5L, 2L));
            assertThat(list).extracting(Genre::getId)
                    .containsExactlyInAnyOrder(2L, 5L);
        }

        @Test
        @DisplayName("should return empty for empty ids")
        void empty() {
            assertThat(repository.findAllByIdIn(Set.of())).isEmpty();
        }
    }
}
