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
        var created = commentService.create(1L, "Great!");
        var loaded = commentService.findById(created.getId()).orElseThrow();
        assertThat(loaded.getText()).isEqualTo("Great!");
        assertThat(loaded.getBook().getTitle()).isEqualTo("BookTitle_1");
    }

    @Test
    @DisplayName("findAllByBookId returns only related comments with book")
    void findAllByBookId() {
        var c = commentService.create(1L, "c1");
        var list = commentService.findAllByBookId(1L);
        assertThat(list).extracting(x -> x.getBook().getId()).contains(1L);
    }

    @Test
    @DisplayName("create should throw when book is missing")
    void createMissingBook() {
        assertThatThrownBy(() -> commentService.create(9999L, "text"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book with id 9999");
    }

    @Test
    @DisplayName("update should throw when comment is missing")
    void updateMissingComment() {
        assertThatThrownBy(() -> commentService.update(8888L, "edited"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Comment with id 8888");
    }

}
