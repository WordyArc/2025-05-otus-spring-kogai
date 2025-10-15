package ru.otus.hw.batch.rdbms2mongo;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import ru.otus.hw.MongoCommonContext;
import ru.otus.hw.batch.rdbms2mongo.idmap.IdMappingRepository;
import ru.otus.hw.persistence.mongo.model.AuthorDocument;
import ru.otus.hw.persistence.mongo.model.BookDocument;
import ru.otus.hw.persistence.mongo.model.CommentDocument;
import ru.otus.hw.persistence.mongo.model.GenreDocument;
import ru.otus.hw.persistence.mongo.repository.MongoAuthorRepository;
import ru.otus.hw.persistence.mongo.repository.MongoBookRepository;
import ru.otus.hw.persistence.mongo.repository.MongoCommentRepository;
import ru.otus.hw.persistence.mongo.repository.MongoGenreRepository;
import ru.otus.hw.persistence.rdbms.model.Author;
import ru.otus.hw.persistence.rdbms.model.Book;
import ru.otus.hw.persistence.rdbms.model.Comment;
import ru.otus.hw.persistence.rdbms.model.Genre;
import ru.otus.hw.persistence.rdbms.repository.AuthorRepository;
import ru.otus.hw.persistence.rdbms.repository.BookRepository;
import ru.otus.hw.persistence.rdbms.repository.CommentRepository;
import ru.otus.hw.persistence.rdbms.repository.GenreRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.batch.core.BatchStatus.COMPLETED;

@SpringBootTest
@SpringBatchTest
class R2mMigrationJobTest extends MongoCommonContext {

    private static final String EXPECTED_JOB_NAME = "rdbmsToMongoJob";
    private static final List<String> MONGO_COLLECTIONS =
            List.of("authors", "genres", "books", "comments", "id_mappings");

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private AuthorRepository relationalAuthors;

    @Autowired
    private GenreRepository relationalGenres;

    @Autowired
    private BookRepository relationalBooks;

    @Autowired
    private CommentRepository relationalComments;


    @Autowired
    private MongoAuthorRepository mongoAuthors;

    @Autowired
    private MongoGenreRepository mongoGenres;

    @Autowired
    private MongoBookRepository mongoBooks;

    @Autowired
    private MongoCommentRepository mongoComments;

    @Autowired
    private IdMappingRepository idMappings;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        jobRepositoryTestUtils.removeJobExecutions();
        cleanRelationalDatabase();
        cleanMongoDatabase();
    }

    private void cleanRelationalDatabase() {
        relationalComments.deleteAll();
        relationalBooks.deleteAll();
        relationalGenres.deleteAll();
        relationalAuthors.deleteAll();
    }

    private void cleanMongoDatabase() {
        MONGO_COLLECTIONS.forEach(collection -> {
            if (mongoTemplate.collectionExists(collection)) {
                mongoTemplate.dropCollection(collection);
            }
        });
    }

    @Nested
    @DisplayName("Migration correctness")
    class MigrationCorrectness {

        @Test
        @DisplayName("Should migrate all authors with correct attributes")
        void shouldMigrateAuthors() throws Exception {
            // given
            var author1 = createAuthor("Isaac Asimov");
            var author2 = createAuthor("Arthur C. Clarke");
            var author3 = createAuthor("Ray Bradbury");

            // when
            JobExecution jobExecution = executeJob();

            // then
            assertJobCompletedSuccessfully(jobExecution);

            // and: all authors are migrated
            assertThat(mongoAuthors.count()).isEqualTo(3);

            // and: author data is preserved
            AuthorDocument migratedAuthor1 = findAuthorByName("Isaac Asimov");
            assertThat(migratedAuthor1.getFullName()).isEqualTo("Isaac Asimov");

            // and: id mappings are created
            verifyIdMapping("author", author1.getId(), migratedAuthor1.getId());
            verifyIdMapping("author", author2.getId(), findAuthorByName("Arthur C. Clarke").getId());
            verifyIdMapping("author", author3.getId(), findAuthorByName("Ray Bradbury").getId());
        }

        @Test
        @DisplayName("Should migrate all genres with correct attributes")
        void shouldMigrateGenres() throws Exception {
            // given
            var genre1 = createGenre("Science Fiction");
            createGenre("Fantasy");
            createGenre("Mystery");

            // when
            JobExecution jobExecution = executeJob();

            // then
            assertJobCompletedSuccessfully(jobExecution);

            // and: all genres are migrated
            assertThat(mongoGenres.count()).isEqualTo(3);

            // and: genre data is preserved
            GenreDocument migratedGenre1 = findGenreByName("Science Fiction");
            assertThat(migratedGenre1.getName()).isEqualTo("Science Fiction");

            // and: id mappings are created
            verifyIdMapping("genre", genre1.getId(), migratedGenre1.getId());
        }

        @Test
        @DisplayName("Should migrate books and preserve author references")
        void shouldMigrateBooksWithAuthorReferences() throws Exception {
            // given
            var author = createAuthor("Frank Herbert");
            var genre1 = createGenre("Sci-Fi");
            var genre2 = createGenre("Adventure");

            var book = createBook("Dune", author, List.of(genre1, genre2));

            // when
            JobExecution jobExecution = executeJob();

            // then
            assertJobCompletedSuccessfully(jobExecution);

            // and: book is migrated with author
            BookDocument migratedBook = findBookByTitle("Dune");
            assertThat(migratedBook.getTitle()).isEqualTo("Dune");
            assertThat(migratedBook.getAuthor()).isNotNull();
            assertThat(migratedBook.getAuthor().getFullName()).isEqualTo("Frank Herbert");

            // and: id mapping is created
            verifyIdMapping("book", book.getId(), migratedBook.getId());
        }

        @Test
        @DisplayName("Should migrate books and preserve genre references")
        void shouldMigrateBooksWithGenreReferences() throws Exception {
            // given
            var author = createAuthor("J. Bloch");
            var genre1 = createGenre("Fantasy");
            var genre2 = createGenre("Epic");
            var genre3 = createGenre("Adventure");

            createBook("Effective Java", author, List.of(genre1, genre2, genre3));

            // when
            executeJob();

            // then: book is migrated with all embedded genres
            BookDocument migratedBook = findBookByTitle("Effective Java");
            assertThat(migratedBook.getGenres())
                    .hasSize(3)
                    .extracting(GenreDocument::getName)
                    .containsExactlyInAnyOrder("Fantasy", "Epic", "Adventure");
        }

        @Test
        @DisplayName("Should migrate comments and preserve book references")
        void shouldMigrateCommentsWithBookReferences() throws Exception {
            // given
            var author = createAuthor("Alfred V. Aho");
            var genre = createGenre("Fantasy");
            var book = createBook("Dragon Book", author, List.of(genre));

            var now = LocalDateTime.now();
            var comment1 = createComment("Great book!", book, now.minusDays(2));
            createComment("Must read", book, now.minusDays(1));
            createComment("Thought-provoking", book, now);

            // when
            executeJob();

            // then: all comments are migrated
            assertThat(mongoComments.count()).isEqualTo(3);

            // and: comments reference the correct book
            BookDocument migratedBook = findBookByTitle("Dragon Book");
            var migratedComments = mongoComments.findAllByBookId(migratedBook.getId());
            
            assertThat(migratedComments).hasSize(3);
            assertThat(migratedComments).allSatisfy(comment -> {
                assertThat(comment.getBookId()).isEqualTo(migratedBook.getId());
                assertThat(comment.getCreatedAt()).isNotNull();
            });

            // and: id mappings are created
            verifyIdMapping("comment", comment1.getId(), 
                    findCommentByText("Great book!").getId());
        }

        @Test
        @DisplayName("Should preserve temporal data during migration")
        void shouldPreserveTemporalData() throws Exception {
            // given
            var author = createAuthor("Test Author");
            var genre = createGenre("Test Genre");
            var book = createBook("Test Book", author, List.of(genre));

            var specificTime = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
            createComment("Timestamped comment", book, specificTime);

            // when
            executeJob();

            // then: temporal data is preserved
            CommentDocument migratedComment = findCommentByText("Timestamped comment");
            assertThat(migratedComment.getCreatedAt()).isEqualTo(specificTime);
        }

        @Test
        @DisplayName("Should create ID mappings for all migrated entities")
        void shouldCreateIdMappingsForAllEntities() throws Exception {
            // given
            var author = createAuthor("Author");
            var genre1 = createGenre("Genre1");
            var genre2 = createGenre("Genre2");
            var book = createBook("Book", author, List.of(genre1, genre2));
            createComment("Comment", book, LocalDateTime.now());

            // when
            executeJob();

            // then
            assertThat(idMappings.count()).isEqualTo(5); // 1 author + 2 genres + 1 book + 1 comment

            // and: mappings can be queried by source type
            assertThat(idMappings.findBySourceTypeAndSourceId("author", String.valueOf(author.getId())))
                    .isPresent();
            assertThat(idMappings.findBySourceTypeAndSourceId("genre", String.valueOf(genre1.getId())))
                    .isPresent();
            assertThat(idMappings.findBySourceTypeAndSourceId("book", String.valueOf(book.getId())))
                    .isPresent();
        }

        @Test
        @DisplayName("Should handle empty source database gracefully")
        void shouldHandleEmptyDatabase() throws Exception {
            // given: empty relational database cleaned in setUp

            // when
            JobExecution jobExecution = executeJob();

            // then
            assertJobCompletedSuccessfully(jobExecution);

            // and: mongo is empty
            assertThat(mongoAuthors.count()).isZero();
            assertThat(mongoGenres.count()).isZero();
            assertThat(mongoBooks.count()).isZero();
            assertThat(mongoComments.count()).isZero();
            assertThat(idMappings.count()).isZero();
        }

        @Test
        @DisplayName("Should migrate large dataset with multiple relationships")
        void shouldMigrateLargeDataset() throws Exception {
            // given: comprehensive dataset
            var author1 = createAuthor("Author_1");
            var author2 = createAuthor("Author_2");
            var author3 = createAuthor("Author_3");

            var genre1 = createGenre("Genre_1");
            var genre2 = createGenre("Genre_2");
            var genre3 = createGenre("Genre_3");
            var genre4 = createGenre("Genre_4");
            var genre5 = createGenre("Genre_5");
            var genre6 = createGenre("Genre_6");

            var book1 = createBook("Book_1", author1, List.of(genre1, genre2));
            var book2 = createBook("Book_2", author2, List.of(genre3, genre4));
            createBook("Book_3", author3, List.of(genre5, genre6));

            var now = LocalDateTime.now();
            createComment("Comment on Book 1 (old)", book1, now.minusDays(1));
            createComment("Comment on Book 1 (new)", book1, now);
            createComment("Comment on Book 2", book2, now);

            // when
            JobExecution jobExecution = executeJob();

            // then
            assertJobCompletedSuccessfully(jobExecution);

            // and
            verifyMigratedEntityCounts(3, 6, 3, 3);

            // and
            BookDocument migratedBook1 = findBookByTitle("Book_1");
            assertThat(migratedBook1.getAuthor().getFullName()).isEqualTo("Author_1");
            assertThat(migratedBook1.getGenres())
                    .extracting(GenreDocument::getName)
                    .containsExactlyInAnyOrder("Genre_1", "Genre_2");

            // and
            verifyCommentsForBook(migratedBook1.getId(), 2);

            // and
            long expectedMappingCount = 3 + 6 + 3 + 3;
            assertThat(idMappings.count()).isEqualTo(expectedMappingCount);
        }
    }

    @Nested
    @DisplayName("Migration idempotency")
    class MigrationIdempotency {

        @Test
        @DisplayName("Should not create duplicate authors on repeated execution")
        void shouldNotDuplicateAuthors() throws Exception {
            // given
            createAuthor("Author_1");
            createAuthor("Author_2");

            // when: job is executed twice
            executeJob();
            Set<ObjectId> authorIdsAfterFirstRun = captureDocumentIds(mongoAuthors.findAll(), AuthorDocument::getId);
            
            executeJob();

            // then: no duplicate authors are created
            assertThat(mongoAuthors.count()).isEqualTo(2);
            assertThat(captureDocumentIds(mongoAuthors.findAll(), AuthorDocument::getId))
                    .isEqualTo(authorIdsAfterFirstRun);
        }

        @Test
        @DisplayName("Should not create duplicate genres on repeated execution")
        void shouldNotDuplicateGenres() throws Exception {
            // given
            createGenre("Genre_1");
            createGenre("Genre_2");
            createGenre("Genre_3");

            // when: job is executed twice
            executeJob();
            Set<ObjectId> genreIdsAfterFirstRun = captureDocumentIds(mongoGenres.findAll(), GenreDocument::getId);
            
            executeJob();

            // then: no duplicate genres are created
            assertThat(mongoGenres.count()).isEqualTo(3);
            assertThat(captureDocumentIds(mongoGenres.findAll(), GenreDocument::getId))
                    .isEqualTo(genreIdsAfterFirstRun);
        }

        @Test
        @DisplayName("Should preserve document IDs across multiple executions")
        void shouldPreserveDocumentIds() throws Exception {
            // given
            var author = createAuthor("Stable Author");
            var genre = createGenre("Stable Genre");
            var book = createBook("Stable Book", author, List.of(genre));
            createComment("Stable Comment", book, LocalDateTime.now());

            // when: job is executed first time
            JobExecution firstExecution = executeJob();
            assertJobCompletedSuccessfully(firstExecution);

            // then: capture ids after first execution
            Set<ObjectId> authorIds = captureDocumentIds(mongoAuthors.findAll(), AuthorDocument::getId);
            Set<ObjectId> genreIds = captureDocumentIds(mongoGenres.findAll(), GenreDocument::getId);
            Set<ObjectId> bookIds = captureDocumentIds(mongoBooks.findAll(), BookDocument::getId);
            Set<ObjectId> commentIds = captureDocumentIds(mongoComments.findAll(), CommentDocument::getId);
            long mappingCount = idMappings.count();

            // when: job is executed second time
            JobExecution secondExecution = executeJob();
            assertJobCompletedSuccessfully(secondExecution);

            // then: all ids remain unchanged
            assertThat(captureDocumentIds(mongoAuthors.findAll(), AuthorDocument::getId)).isEqualTo(authorIds);
            assertThat(captureDocumentIds(mongoGenres.findAll(), GenreDocument::getId)).isEqualTo(genreIds);
            assertThat(captureDocumentIds(mongoBooks.findAll(), BookDocument::getId)).isEqualTo(bookIds);
            assertThat(captureDocumentIds(mongoComments.findAll(), CommentDocument::getId)).isEqualTo(commentIds);

            // and: id mappings count remains the same
            assertThat(idMappings.count()).isEqualTo(mappingCount);
        }

        @Test
        @DisplayName("Should maintain referential integrity on repeated execution")
        void shouldMaintainReferentialIntegrity() throws Exception {
            // given
            var author = createAuthor("Reference Author");
            var genre = createGenre("Reference Genre");
            var book = createBook("Reference Book", author, List.of(genre));
            createComment("Reference Comment", book, LocalDateTime.now());

            // when: job is executed twice
            executeJob();
            BookDocument bookAfterFirstRun = findBookByTitle("Reference Book");
            ObjectId authorIdAfterFirstRun = bookAfterFirstRun.getAuthor().getId();
            ObjectId genreIdAfterFirstRun = bookAfterFirstRun.getGenres().get(0).getId();
            
            executeJob();

            // then: embedded references remain unchanged
            BookDocument bookAfterSecondRun = findBookByTitle("Reference Book");
            assertThat(bookAfterSecondRun.getAuthor().getId()).isEqualTo(authorIdAfterFirstRun);
            assertThat(bookAfterSecondRun.getGenres().get(0).getId()).isEqualTo(genreIdAfterFirstRun);
        }
    }

    @Nested
    @DisplayName("Job execution characteristics")
    class JobExecutionCharacteristics {

        @Test
        @DisplayName("Should complete with COMPLETED status for valid data")
        void shouldCompleteSuccessfully() throws Exception {
            // given
            var author = createAuthor("Test Author");
            var genre = createGenre("Test Genre");
            createBook("Test Book", author, List.of(genre));

            // when
            JobExecution jobExecution = executeJob();

            // then: job completes with COMPLETED status
            assertThat(jobExecution.getStatus()).isEqualTo(COMPLETED);
            assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
            assertThat(jobExecution.getJobInstance().getJobName()).isEqualTo(EXPECTED_JOB_NAME);
        }

        @Test
        @DisplayName("Should execute with unique job parameters for each run")
        void shouldAcceptUniqueJobParameters() throws Exception {
            // given
            createAuthor("Author");

            // when
            JobExecution firstExecution = executeJob();
            JobExecution secondExecution = executeJob();

            // then: both executions complete successfully
            assertThat(firstExecution.getStatus()).isEqualTo(COMPLETED);
            assertThat(secondExecution.getStatus()).isEqualTo(COMPLETED);

            // and: they are different executions
            assertThat(firstExecution.getId()).isNotEqualTo(secondExecution.getId());
        }
    }

    private Author createAuthor(String name) {
        return relationalAuthors.save(new Author(null, name));
    }

    private Genre createGenre(String name) {
        return relationalGenres.save(new Genre(null, name));
    }

    private Book createBook(String title, Author author, List<Genre> genres) {
        return relationalBooks.save(new Book(null, title, author, List.copyOf(genres)));
    }

    private Comment createComment(String text, Book book, LocalDateTime createdAt) {
        return relationalComments.save(new Comment(null, text, book, createdAt));
    }

    private JobExecution executeJob() throws Exception {
        return jobLauncherTestUtils.launchJob(
                new JobParametersBuilder()
                        .addLong("timestamp", System.currentTimeMillis())
                        .toJobParameters()
        );
    }

    private void assertJobCompletedSuccessfully(JobExecution jobExecution) {
        assertThat(jobExecution.getJobInstance().getJobName()).isEqualTo(EXPECTED_JOB_NAME);
        assertThat(jobExecution.getStatus()).isEqualTo(COMPLETED);
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
    }

    private void verifyMigratedEntityCounts(long authors, long genres, long books, long comments) {
        assertThat(mongoAuthors.count()).isEqualTo(authors);
        assertThat(mongoGenres.count()).isEqualTo(genres);
        assertThat(mongoBooks.count()).isEqualTo(books);
        assertThat(mongoComments.count()).isEqualTo(comments);
    }

    private BookDocument findBookByTitle(String title) {
        return mongoBooks.findAll().stream()
                .filter(book -> title.equals(book.getTitle()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Book not found: " + title));
    }

    private AuthorDocument findAuthorByName(String name) {
        return mongoAuthors.findAll().stream()
                .filter(author -> name.equals(author.getFullName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Author not found: " + name));
    }

    private GenreDocument findGenreByName(String name) {
        return mongoGenres.findAll().stream()
                .filter(genre -> name.equals(genre.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Genre not found: " + name));
    }

    private CommentDocument findCommentByText(String text) {
        return mongoComments.findAll().stream()
                .filter(comment -> text.equals(comment.getText()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Comment not found: " + text));
    }

    private void verifyCommentsForBook(ObjectId bookId, int expectedCount) {
        var comments = mongoComments.findAllByBookId(bookId);
        assertThat(comments).hasSize(expectedCount);
        assertThat(comments).allSatisfy(comment -> {
            assertThat(comment.getBookId()).isEqualTo(bookId);
            assertThat(comment.getCreatedAt()).isNotNull();
        });
    }

    private void verifyIdMapping(String entityType, Long sourceId, ObjectId targetId) {
        var mapping = idMappings.findBySourceTypeAndSourceId(entityType, String.valueOf(sourceId))
                .orElseThrow(() -> new AssertionError("Mapping not found for " + entityType + ":" + sourceId));
        assertThat(mapping.getTargetId()).isEqualTo(targetId);
    }

    private <T> Set<ObjectId> captureDocumentIds(List<T> documents, Function<T, ObjectId> idExtractor) {
        return documents.stream()
                .map(idExtractor)
                .collect(toSet());
    }
}
