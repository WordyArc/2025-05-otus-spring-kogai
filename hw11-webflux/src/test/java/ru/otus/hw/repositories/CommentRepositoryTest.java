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
import ru.otus.hw.models.Comment;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
class CommentRepositoryTest extends CommonContext {

    @Autowired
    CommentRepository repository;

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
    @DisplayName("save creates comment")
    void create() {
        repository.save(new Comment(null, "Nice", 1L, LocalDateTime.now()))
                .as(StepVerifier::create)
                .assertNext(saved -> assertThat(saved.getId()).isNotNull())
                .verifyComplete();
    }

    @Nested
    @DisplayName("findById")
    class FindById {
        @Test
        @DisplayName("returns existing comment")
        void existing() {
            repository.save(new Comment(null, "A", 1L, LocalDateTime.now()))
                    .flatMap(c -> repository.findById(c.getId()))
                    .as(StepVerifier::create)
                    .assertNext(found -> assertThat(found.getText()).isEqualTo("A"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("returns empty when not found")
        void missing() {
            repository.findById(123456L)
                    .as(StepVerifier::create)
                    .verifyComplete();
        }
    }

    @Test
    @DisplayName("findAllByBookId returns only comments for that book")
    void findAllByBook() {
        repository.save(new Comment(null, "c1", 1L, LocalDateTime.now()))
                .thenMany(repository.save(new Comment(null, "c2", 2L, LocalDateTime.now())))
                .thenMany(repository.findAllByBookId(1L))
                .as(StepVerifier::create)
                .thenConsumeWhile(c -> {
                    assertThat(c.getBookId()).isEqualTo(1L);
                    return true;
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("save updates text for existing comment")
    void update() {
        repository.save(new Comment(null, "Text", 1L, LocalDateTime.now()))
                .flatMap(c -> {
                    c.setText("Edited");
                    return repository.save(c).then(repository.findById(c.getId()));
                })
                .as(StepVerifier::create)
                .assertNext(updated -> assertThat(updated.getText()).isEqualTo("Edited"))
                .verifyComplete();
    }

    @Test
    @DisplayName("deleteById is idempotent")
    void delete() {
        repository.save(new Comment(null, "Text", 1L, LocalDateTime.now()))
                .flatMap(c -> repository.deleteById(c.getId()).then(repository.findById(c.getId())))
                .as(StepVerifier::create)
                .verifyComplete();
    }

}
