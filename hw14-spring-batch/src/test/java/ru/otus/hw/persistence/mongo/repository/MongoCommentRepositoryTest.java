package ru.otus.hw.persistence.mongo.repository;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import ru.otus.hw.persistence.mongo.model.AuthorDocument;
import ru.otus.hw.persistence.mongo.model.BookDocument;
import ru.otus.hw.persistence.mongo.model.CommentDocument;
import ru.otus.hw.persistence.mongo.model.GenreDocument;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class MongoCommentRepositoryTest {

    @Autowired
    private MongoCommentRepository commentRepository;

    @Autowired
    private MongoBookRepository bookRepository;

    @Autowired
    private MongoAuthorRepository authorRepository;

    @Autowired
    private MongoGenreRepository genreRepository;

    private ObjectId book1Id;
    private ObjectId book2Id;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        genreRepository.deleteAll();

        var a1 = authorRepository.save(new AuthorDocument(null, "Author_1"));
        var a2 = authorRepository.save(new AuthorDocument(null, "Author_2"));
        var g1 = genreRepository.save(new GenreDocument(null, "Genre_1"));
        var g2 = genreRepository.save(new GenreDocument(null, "Genre_2"));
        var g3 = genreRepository.save(new GenreDocument(null, "Genre_3"));
        var g4 = genreRepository.save(new GenreDocument(null, "Genre_4"));

        var book1 = bookRepository.save(new BookDocument(null, "BookTitle_1", a1, List.of(g1, g2)));
        var book2 = bookRepository.save(new BookDocument(null, "BookTitle_2", a2, List.of(g3, g4)));
        
        book1Id = book1.getId();
        book2Id = book2.getId();
    }

    @Test
    @DisplayName("save should create comment")
    void create() {
        var saved = commentRepository.save(new CommentDocument(null, "Nice", book1Id, LocalDateTime.now(), null));
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
            var saved = commentRepository.save(new CommentDocument(null, "A", book1Id, LocalDateTime.now(), null));
            assertThat(commentRepository.findById(saved.getId())).isPresent();
        }

        @Test
        @DisplayName("should return empty when not found")
        void missing() {
            assertThat(commentRepository.findById(new ObjectId())).isEmpty();
        }
    }

    @Test
    @DisplayName("findAllByBookId should return only comments for that book")
    void findAllByBook() {
        commentRepository.save(new CommentDocument(null, "c1", book1Id, LocalDateTime.now(), null));
        commentRepository.save(new CommentDocument(null, "c2", book2Id, LocalDateTime.now(), null));

        var c1 = commentRepository.findAllByBookId(book1Id);
        assertThat(c1).allMatch(c -> c.getBookId().equals(book1Id));

        var empty = commentRepository.findAllByBookId(new ObjectId());
        assertThat(empty).isEmpty();
    }

    @Test
    @DisplayName("save should update text for existing comment")
    void update() {
        var saved = commentRepository.save(new CommentDocument(null, "Text", book1Id, LocalDateTime.now(), null));
        saved.setText("Edited");
        commentRepository.save(saved);

        assertThat(commentRepository.findById(saved.getId()))
                .get().extracting(CommentDocument::getText).isEqualTo("Edited");
    }

    @Test
    @DisplayName("deleteById should be idempotent")
    void delete() {
        var saved = commentRepository.save(new CommentDocument(null, "Text", book1Id, LocalDateTime.now(), null));
        commentRepository.deleteById(saved.getId());
        assertThat(commentRepository.findById(saved.getId())).isEmpty();
    }

}
