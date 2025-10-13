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
import ru.otus.hw.models.Author;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
class AuthorRepositoryTest extends CommonContext {

    @Autowired
    private AuthorRepository repository;

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
    @DisplayName("findAll returns all authors")
    void findAll() {
        repository.findAll()
                .collectList()
                .as(StepVerifier::create)
                .assertNext(list -> {
                    assertThat(list)
                            .extracting(Author::getId)
                            .containsExactlyInAnyOrder(1L, 2L, 3L);
                    assertThat(list)
                            .extracting(Author::getFullName)
                            .containsExactlyInAnyOrder("Author_1", "Author_2", "Author_3");
                })
                .verifyComplete();
    }

    @Nested
    @DisplayName("findById")
    class FindById {
        @Test
        @DisplayName("returns author when exists")
        void returnsAuthor() {
            repository.findById(2L)
                    .as(StepVerifier::create)
                    .assertNext(a -> assertThat(a.getFullName()).isEqualTo("Author_2"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("returns empty when not exists")
        void returnsEmpty() {
            repository.findById(999L)
                    .as(StepVerifier::create)
                    .verifyComplete();
        }
    }
}
