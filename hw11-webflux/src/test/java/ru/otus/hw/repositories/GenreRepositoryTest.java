package ru.otus.hw.repositories;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import reactor.test.StepVerifier;
import ru.otus.hw.CommonContext;
import ru.otus.hw.TestData;
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
class GenreRepositoryTest extends CommonContext {

    @Autowired
    private GenreRepository repository;

    @Autowired
    protected TestData data;

    @BeforeEach
    void setUp() {
        data.resetAndSeed();
    }

    @AfterEach
    void tearDown() {
        data.cleanAll();
    }

    @Test
    @DisplayName("findAll returns all genres")
    void shouldFindAll() {
        repository.findAll().collectList()
                .as(StepVerifier::create)
                .assertNext(list -> {
                    assertThat(list)
                            .extracting(Genre::getId)
                            .containsExactlyInAnyOrder(1L,2L,3L,4L,5L,6L);
                    assertThat(list)
                            .extracting(Genre::getName)
                            .containsExactlyInAnyOrder("Genre_1","Genre_2","Genre_3","Genre_4","Genre_5","Genre_6");
                })
                .verifyComplete();
    }

    @Nested
    @DisplayName("findAllById")
    class FindAllByIds {
        @Test
        @DisplayName("returns exactly requested ids")
        void ordered() {
            repository.findAllById(List.of(5L, 2L))
                    .collectList()
                    .as(StepVerifier::create)
                    .assertNext(list -> assertThat(list)
                            .extracting(Genre::getId)
                            .containsExactlyInAnyOrder(2L, 5L))
                    .verifyComplete();
        }

        @Test
        @DisplayName("returns empty for empty ids")
        void empty() {
            repository.findAllById(List.<Long>of())
                    .as(StepVerifier::create)
                    .verifyComplete();
        }
    }
}
