package ru.otus.hw.migrations;

import com.mongodb.MongoWriteException;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import ru.otus.hw.MongoIntegrationTest;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;
import ru.otus.hw.utils.MigrationUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class MongockMigrationTest extends MongoIntegrationTest {

    @Autowired
    private MongoTemplate mongo;
    @Autowired
    private AuthorRepository authors;
    @Autowired
    private GenreRepository genres;
    @Autowired
    private BookRepository books;
    @Autowired
    private CommentRepository comments;

    @BeforeEach
    void clean() {
        comments.deleteAll();
        books.deleteAll();
        authors.deleteAll();
        genres.deleteAll();
    }

    @Test
    @DisplayName("Collections are created by migration")
    void collectionsCreated() {
        assertThat(MigrationUtils.collectionExists(mongo, "authors")).isTrue();
        assertThat(MigrationUtils.collectionExists(mongo, "genres")).isTrue();
        assertThat(MigrationUtils.collectionExists(mongo, "books")).isTrue();
        assertThat(MigrationUtils.collectionExists(mongo, "comments")).isTrue();
    }

    @Nested
    class AuthorsSchema {

        @Test
        @DisplayName("accepts valid author")
        void acceptsValid() {
            var saved = authors.save(new Author("a1", "Good Name"));
            assertThat(saved.getId()).isEqualTo("a1");
        }

        @Test
        @DisplayName("rejects empty fullName")
        void rejectsEmptyFullName() {
            var doc = new Document().append("_id", "bad").append("fullName", "");
            assertThatThrownBy(() -> insert("authors", doc))
                    .isInstanceOf(MongoWriteException.class)
                    .hasMessageContaining("Document failed validation");
        }
    }

    @Nested
    class GenresSchema {

        @Test
        @DisplayName("accepts valid genre")
        void acceptsValid() {
            var saved = genres.save(new Genre("g1", "Genre"));
            assertThat(saved.getId()).isEqualTo("g1");
        }

        @Test
        @DisplayName("rejects empty name")
        void rejectsEmptyName() {
            var doc = new Document().append("_id", "bad").append("name", "");
            assertThatThrownBy(() -> insert("genres", doc))
                    .isInstanceOf(MongoWriteException.class)
                    .hasMessageContaining("Document failed validation");
        }
    }

    @Nested
    class BooksSchema {

        private Author a1;
        private Genre g1;
        private Genre g2;

        @BeforeEach
        void seedRefs() {
            a1 = authors.save(new Author("a1", "Author_1"));
            g1 = genres.save(new Genre("g1", "Genre_1"));
            g2 = genres.save(new Genre("g2", "Genre_2"));
        }

        @Test
        @DisplayName("accepts valid book with @DBRef author and genres")
        void acceptsValid() {
            var saved = books.save(new Book("b1", "T", a1, List.of(g1, g2)));
            assertThat(saved.getId()).isEqualTo("b1");
        }

        @Test
        @DisplayName("rejects missing 'author' field")
        void rejectsMissingAuthor() {
            var doc = new Document()
                    .append("_id", "b-missing-author")
                    .append("title", "T");
            assertThatThrownBy(() -> insert("books", doc))
                    .isInstanceOf(MongoWriteException.class)
                    .hasMessageContaining("Document failed validation");
        }

        @Test
        @DisplayName("rejects 'author' not in DBRef shape")
        void rejectsAuthorWrongShape() {
            var doc = new Document()
                    .append("_id", "b-wrong-author")
                    .append("title", "T")
                    .append("author", "a1");
            assertThatThrownBy(() -> insert("books", doc))
                    .isInstanceOf(MongoWriteException.class)
                    .hasMessageContaining("Document failed validation");
        }

        @Test
        @DisplayName("rejects 'genres' not in DBRef shape")
        void rejectsGenresWrongShape() {
            var doc = new Document()
                    .append("_id", "b-wrong-genres")
                    .append("title", "T")
                    .append("author", dbRef("authors", a1.getId()))
                    .append("genres", List.of("g1", "g2"));
            assertThatThrownBy(() -> insert("books", doc))
                    .isInstanceOf(MongoWriteException.class)
                    .hasMessageContaining("Document failed validation");
        }

        private Document dbRef(String collection, String id) {
            return new Document()
                    .append("$ref", collection)
                    .append("$id", id);
        }
    }

    @Nested
    class CommentsSchema {

        private Author a1;
        private Genre g1;
        private Book b1;

        @BeforeEach
        void seedBook() {
            a1 = authors.save(new Author("a1", "Author_1"));
            g1 = genres.save(new Genre("g1", "Genre_1"));
            b1 = books.save(new Book("b1", "Book", a1, List.of(g1)));
        }

        @Test
        @DisplayName("accepts valid comment")
        void acceptsValid() {
            var saved = comments.save(new ru.otus.hw.models.Comment(
                    null, "Nice", b1.getId(), LocalDateTime.now(), null
            ));
            assertThat(saved.getId()).isNotNull();
        }

        @Test
        @DisplayName("rejects missing createdAt")
        void rejectsMissingCreatedAt() {
            var doc = new Document()
                    .append("text", "t")
                    .append("bookId", b1.getId());
            assertThatThrownBy(() -> insert("comments", doc))
                    .isInstanceOf(MongoWriteException.class)
                    .hasMessageContaining("Document failed validation");
        }

        @Test
        @DisplayName("rejects empty text")
        void rejectsEmptyText() {
            var doc = new Document()
                    .append("text", "")
                    .append("bookId", b1.getId())
                    .append("createdAt", java.util.Date.from(java.time.Instant.now()));
            assertThatThrownBy(() -> insert("comments", doc))
                    .isInstanceOf(MongoWriteException.class)
                    .hasMessageContaining("Document failed validation");
        }

        @Test
        @DisplayName("rejects wrong createdAt type")
        void rejectsWrongCreatedAtType() {
            var doc = new Document()
                    .append("text", "ok")
                    .append("bookId", b1.getId())
                    .append("createdAt", "2024-01-01T10:00:00");
            assertThatThrownBy(() -> insert("comments", doc))
                    .isInstanceOf(MongoWriteException.class)
                    .hasMessageContaining("Document failed validation");
        }
    }

    private void insert(String collection, Document doc) {
        mongo.getCollection(collection).insertOne(doc);
    }

}
