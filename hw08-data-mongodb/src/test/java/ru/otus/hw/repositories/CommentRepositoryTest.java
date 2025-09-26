package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import ru.otus.hw.CommonContext;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class CommentRepositoryTest extends CommonContext {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        genreRepository.deleteAll();

        var a1 = authorRepository.save(new Author("a1", "Author_1"));
        var a2 = authorRepository.save(new Author("a2", "Author_2"));
        var g1 = genreRepository.save(new Genre("g1", "Genre_1"));
        var g2 = genreRepository.save(new Genre("g2", "Genre_2"));
        var g3 = genreRepository.save(new Genre("g3", "Genre_3"));
        var g4 = genreRepository.save(new Genre("g4", "Genre_4"));

        bookRepository.save(new Book("b1", "BookTitle_1", a1, List.of(g1, g2)));
        bookRepository.save(new Book("b2", "BookTitle_2", a2, List.of(g3, g4)));
    }

    @Test
    @DisplayName("save should create comment")
    void create() {
        var saved = commentRepository.save(new Comment(null, "Nice", "b1", LocalDateTime.now(), null));
        assertThat(saved.getId()).isNotNull();

        var reloaded = commentRepository.findById(saved.getId());
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getText()).isEqualTo("Nice");
    }

    @Nested
    @DisplayName("findById")
    class FindById {
        @Test
        @DisplayName("should return existing comment")
        void existing() {
            var saved = commentRepository.save(new Comment(null, "A", "b1", LocalDateTime.now(), null));
            assertThat(commentRepository.findById(saved.getId())).isPresent();
        }

        @Test
        @DisplayName("should return empty when not found")
        void missing() {
            assertThat(commentRepository.findById("missing")).isEmpty();
        }
    }

    @Test
    @DisplayName("findAllByBookId should return only comments for that book")
    void findAllByBook() {
        commentRepository.save(new Comment(null, "c1", "b1", LocalDateTime.now(), null));
        commentRepository.save(new Comment(null, "c2", "b2", LocalDateTime.now(), null));

        var c1 = commentRepository.findAllByBookId("b1");
        assertThat(c1).allMatch(c -> c.getBookId().equals("b1"));

        var empty = commentRepository.findAllByBookId("no-book");
        assertThat(empty).isEmpty();
    }

    @Test
    @DisplayName("save should update text for existing comment")
    void update() {
        var saved = commentRepository.save(new Comment(null, "Text", "b1", LocalDateTime.now(), null));
        saved.setText("Edited");
        commentRepository.save(saved);

        assertThat(commentRepository.findById(saved.getId()))
                .get().extracting(Comment::getText).isEqualTo("Edited");
    }

    @Test
    @DisplayName("deleteById should be idempotent")
    void delete() {
        var saved = commentRepository.save(new Comment(null, "Text", "b1", LocalDateTime.now(), null));
        commentRepository.deleteById(saved.getId());
        assertThat(commentRepository.findById(saved.getId())).isEmpty();
    }

}
