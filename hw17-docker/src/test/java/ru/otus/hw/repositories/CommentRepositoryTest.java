package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("save should create comment")
    void create() {
        var book = persistBook("create-book");
        var saved = commentRepository.save(new Comment(null, "Nice", book, LocalDateTime.now()));
        assertThat(saved.getId()).isNotNull();

        entityManager.flush();
        entityManager.clear();
        Optional<Comment> reloadedComment = commentRepository.findById(saved.getId());
        assertThat(reloadedComment).isPresent();
        assertThat(reloadedComment.get().getText()).isEqualTo("Nice");
    }

    @Nested
    @DisplayName("findById")
    class FindById {
        @Test
        @DisplayName("should return existing comment")
        void existing() {
            var book = persistBook("find-existing-book");
            var saved = commentRepository.save(new Comment(null, "A", book, LocalDateTime.now()));
            assertThat(commentRepository.findById(saved.getId())).isPresent();
        }

        @Test
        @DisplayName("should return empty when not found")
        void missing() {
            assertThat(commentRepository.findById(123456L)).isEmpty();
        }
    }

    @Test
    @DisplayName("findAllByBookId should return only comments for that book")
    void findAllByBook() {
        var book1 = persistBook("book-1");
        var book2 = persistBook("book-2");
        commentRepository.save(new Comment(null, "c1", book1, LocalDateTime.now()));
        commentRepository.save(new Comment(null, "c2", book2, LocalDateTime.now()));

        var commentsForBook1 = commentRepository.findAllByBookId(book1.getId());
        assertThat(commentsForBook1)
                .hasSize(1)
                .allMatch(c -> c.getBook().getId().equals(book1.getId()));

        var empty = commentRepository.findAllByBookId(9999L);
        assertThat(empty).isEmpty();
    }

    @Test
    @DisplayName("save should update text for existing comment")
    void update() {
        var book = persistBook("update-book");
        var saved = commentRepository.save(new Comment(null, "Text", book, LocalDateTime.now()));
        saved.setText("Edited");
        commentRepository.save(saved);

        assertThat(commentRepository.findById(saved.getId())).get().extracting(Comment::getText).isEqualTo("Edited");
    }

    @Test
    @DisplayName("deleteById should be idempotent")
    void delete() {
        var book = persistBook("delete-book");
        var saved = commentRepository.save(new Comment(null, "Text", book, LocalDateTime.now()));
        commentRepository.deleteById(saved.getId());
        assertThat(commentRepository.findById(saved.getId())).isEmpty();
    }

    private Book persistBook(String title) {
        var author = entityManager.persist(new Author(null, "Author-" + title));
        var genre = entityManager.persist(new Genre(null, "Genre-" + title));

        var book = new Book(null, title, author, new ArrayList<>());
        book.getGenres().add(genre);

        return entityManager.persistAndFlush(book);
    }
}
