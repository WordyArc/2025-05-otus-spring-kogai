package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.otus.hw.exceptions.EntityNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

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

}
