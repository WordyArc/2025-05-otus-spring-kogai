package ru.otus.hw.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;
import ru.otus.hw.CommonContext;
import ru.otus.hw.TestData;
import ru.otus.hw.exceptions.EntityNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CommentServiceTest extends CommonContext {

    @Autowired
    CommentService commentService;

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
    @DisplayName("create & findById returns persisted comment")
    void createAndRead() {
        commentService.create(1L, "Great!")
                .flatMap(c -> commentService.findById(c.getId()))
                .as(StepVerifier::create)
                .assertNext(loaded -> assertThat(loaded.getText()).isEqualTo("Great!"))
                .verifyComplete();
    }

    @Test
    @DisplayName("findAllByBookId returns only related comments")
    void findAllByBookId() {
        commentService.create(1L, "c1")
                .thenMany(commentService.findAllByBookId(1L))
                .as(StepVerifier::create)
                .thenConsumeWhile(c -> {
                    assertThat(c.getBookId()).isEqualTo(1L);
                    return true;
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("create should error when book is missing")
    void createMissingBook() {
        commentService.create(9999L, "text")
                .as(StepVerifier::create)
                .expectErrorSatisfies(e -> assertThat(e)
                        .isInstanceOf(EntityNotFoundException.class)
                        .hasMessageContaining("Book with id 9999"))
                .verify();
    }

    @Test
    @DisplayName("update should error when comment is missing")
    void updateMissingComment() {
        commentService.update(8888L, "edited")
                .as(StepVerifier::create)
                .expectErrorSatisfies(e -> assertThat(e)
                        .isInstanceOf(EntityNotFoundException.class)
                        .hasMessageContaining("Comment with id 8888"))
                .verify();
    }

}
