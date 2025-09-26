package ru.otus.hw.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.CommonContext;
import ru.otus.hw.TestData;
import ru.otus.hw.config.TestDataConfig;
import ru.otus.hw.exceptions.EntityNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestDataConfig.class)
class CommentServiceTest extends CommonContext {

    @Autowired
    private CommentService commentService;

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
    @DisplayName("create & findById returns comment with attached book")
    void createAndRead() {
        var created = commentService.create("b1", "Great!");
        var loaded = commentService.findById(created.getId()).orElseThrow();
        assertThat(loaded.getText()).isEqualTo("Great!");
        assertThat(loaded.getBook()).isNotNull();
        assertThat(loaded.getBook().getTitle()).isEqualTo("BookTitle_1");
    }

    @Test
    @DisplayName("findAllByBookId returns only related comments with book")
    void findAllByBookId() {
        commentService.create("b1", "c1");
        var list = commentService.findAllByBookId("b1");
        assertThat(list).allMatch(c -> "b1".equals(c.getBookId()));
        assertThat(list).allSatisfy(c -> assertThat(c.getBook()).isNotNull());
    }

    @Test
    @DisplayName("create should throw when book is missing")
    void createMissingBook() {
        assertThatThrownBy(() -> commentService.create("bad", "text"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book with id bad");
    }

    @Test
    @DisplayName("update should throw when comment is missing")
    void updateMissingComment() {
        assertThatThrownBy(() -> commentService.update("missing", "edited"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Comment with id missing");
    }

    @Test
    @DisplayName("create returns a comment with attached book")
    void createReturnsAttachedBook() {
        var created = commentService.create("b1", "hello");
        assertThat(created.getId()).isNotNull();
        assertThat(created.getBook()).isNotNull();
        assertThat(created.getBook().getTitle()).isEqualTo("BookTitle_1");
    }

    @Test
    @DisplayName("update changes text and keeps attached book")
    void updateSuccess() {
        var created = commentService.create("b1", "old");
        var updated = commentService.update(created.getId(), "new");

        assertThat(updated.getText()).isEqualTo("new");
        assertThat(updated.getBook()).isNotNull();
        assertThat(updated.getBook().getId()).isEqualTo("b1");
    }

    @Test
    @DisplayName("deleteById removes existing comment")
    void deleteExisting() {
        var created = commentService.create("b1", "to delete");
        commentService.deleteById(created.getId());
        assertThat(commentService.findById(created.getId())).isEmpty();
    }

    @Test
    @DisplayName("findById returns empty for missing comment")
    void findByIdMissing() {
        assertThat(commentService.findById("missing")).isEmpty();
    }

}
