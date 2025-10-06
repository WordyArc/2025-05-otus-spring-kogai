package ru.otus.hw.migration;

import com.mongodb.MongoWriteException;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import ru.otus.hw.MongoCommonContext;
import ru.otus.hw.persistence.mongo.model.AuthorDocument;
import ru.otus.hw.persistence.mongo.model.BookDocument;
import ru.otus.hw.persistence.mongo.model.CommentDocument;
import ru.otus.hw.persistence.mongo.model.GenreDocument;
import ru.otus.hw.persistence.mongo.repository.MongoAuthorRepository;
import ru.otus.hw.persistence.mongo.repository.MongoBookRepository;
import ru.otus.hw.persistence.mongo.repository.MongoCommentRepository;
import ru.otus.hw.persistence.mongo.repository.MongoGenreRepository;
import ru.otus.hw.util.MongoMigrationUtils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
class MongockMigrationTest extends MongoCommonContext {

    @Autowired
    private MongoTemplate mongo;
    @Autowired
    private MongoAuthorRepository authors;
    @Autowired
    private MongoGenreRepository genres;
    @Autowired
    private MongoBookRepository books;
    @Autowired
    private MongoCommentRepository comments;

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
        assertThat(MongoMigrationUtils.collectionExists(mongo, "authors")).isTrue();
        assertThat(MongoMigrationUtils.collectionExists(mongo, "genres")).isTrue();
        assertThat(MongoMigrationUtils.collectionExists(mongo, "books")).isTrue();
        assertThat(MongoMigrationUtils.collectionExists(mongo, "comments")).isTrue();
    }

    @Nested
    class AuthorsSchema {

        @Test
        @DisplayName("accepts valid author")
        void acceptsValid() {
            var saved = authors.save(new AuthorDocument(null, "Good Name"));
            assertThat(saved.getId()).isNotNull();
        }

        @Test
        @DisplayName("rejects empty fullName")
        void rejectsEmptyFullName() {
            var doc = new Document().append("_id", new ObjectId()).append("fullName", "");
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
            var saved = genres.save(new GenreDocument(null, "Genre"));
            assertThat(saved.getId()).isNotNull();
        }

        @Test
        @DisplayName("rejects empty name")
        void rejectsEmptyName() {
            var doc = new Document().append("_id", new ObjectId()).append("name", "");
            assertThatThrownBy(() -> insert("genres", doc))
                    .isInstanceOf(MongoWriteException.class)
                    .hasMessageContaining("Document failed validation");
        }
    }

    @Nested
    class BooksSchema {

        private AuthorDocument a1;
        private GenreDocument g1;
        private GenreDocument g2;

        @BeforeEach
        void seedRefs() {
            a1 = authors.save(new AuthorDocument(null, "Author_1"));
            g1 = genres.save(new GenreDocument(null, "Genre_1"));
            g2 = genres.save(new GenreDocument(null, "Genre_2"));
        }

        @Test
        @DisplayName("accepts valid book with @DBRef author and genres")
        void acceptsValid() {
            var saved = books.save(new BookDocument(null, "T", a1, List.of(g1, g2)));
            assertThat(saved.getId()).isNotNull();
        }

        @Test
        @DisplayName("rejects missing 'author' field")
        void rejectsMissingAuthor() {
            var doc = new Document()
                    .append("_id", new ObjectId())
                    .append("title", "T");
            assertThatThrownBy(() -> insert("books", doc))
                    .isInstanceOf(MongoWriteException.class)
                    .hasMessageContaining("Document failed validation");
        }

        @Test
        @DisplayName("rejects 'author' not in DBRef shape")
        void rejectsAuthorWrongShape() {
            var doc = new Document()
                    .append("_id", new ObjectId())
                    .append("title", "T")
                    .append("author", a1.getId());
            assertThatThrownBy(() -> insert("books", doc))
                    .isInstanceOf(MongoWriteException.class)
                    .hasMessageContaining("Document failed validation");
        }

        @Test
        @DisplayName("rejects 'genres' not in DBRef shape")
        void rejectsGenresWrongShape() {
            var doc = new Document()
                    .append("_id", new ObjectId())
                    .append("title", "T")
                    .append("author", dbRef("authors", a1.getId()))
                    .append("genres", List.of(g1.getId(), g2.getId()));
            assertThatThrownBy(() -> insert("books", doc))
                    .isInstanceOf(MongoWriteException.class)
                    .hasMessageContaining("Document failed validation");
        }

        private Document dbRef(String collection, ObjectId id) {
            return new Document()
                    .append("$ref", collection)
                    .append("$id", id);
        }
    }

    @Nested
    class CommentsSchema {

        private AuthorDocument a1;
        private GenreDocument g1;
        private BookDocument b1;

        @BeforeEach
        void seedBook() {
            a1 = authors.save(new AuthorDocument(null, "Author_1"));
            g1 = genres.save(new GenreDocument(null, "Genre_1"));
            b1 = books.save(new BookDocument(null, "Book", a1, List.of(g1)));
        }

        @Test
        @DisplayName("accepts valid comment")
        void acceptsValid() {
            var saved = comments.save(new CommentDocument(
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
                    .append("createdAt", Date.from(java.time.Instant.now()));
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
