package ru.otus.hw.batch.rdbms2mongo;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    @Test
    @DisplayName("Should successfully migrate all data from RDBMS to MongoDB with correct mappings")
    void shouldMigrateAllDataWithCorrectMappings() throws Exception {
        // given
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

        // when: migration job is executed
        JobExecution jobExecution = executeJob();

        // then: job completes successfully
        assertJobCompletedSuccessfully(jobExecution);

        // and: all entities are migrated
        verifyMigratedEntityCounts(3, 6, 3, 3);

        // and: book references are correctly mapped
        BookDocument migratedBook1 = findBookByTitle("Book_1");
        assertThat(migratedBook1.getAuthor().getFullName()).isEqualTo("Author_1");
        assertThat(migratedBook1.getGenres())
                .extracting(GenreDocument::getName)
                .containsExactlyInAnyOrder("Genre_1", "Genre_2");

        // and: comment references are correctly mapped
        verifyCommentsForBook(migratedBook1.getId(), 2);

        // and: ID mappings are consistent
        verifyIdMapping("book", book1.getId(), migratedBook1.getId());

        AuthorDocument migratedAuthor2 = findAuthorByName("Author_2");
        verifyIdMapping("author", author2.getId(), migratedAuthor2.getId());

        // and: all ID mappings are created
        long expectedMappingCount = 3 + 6 + 3 + 3; // authors + genres + books + comments
        assertThat(idMappings.count()).isEqualTo(expectedMappingCount);
    }

    @Test
    @DisplayName("Should be idempotent: repeated execution preserves document IDs and prevents duplicates")
    void shouldBeIdempotentOnRepeatedExecution() throws Exception {
        // given: relational database with test data
        var author = createAuthor("Single Author");
        var genre = createGenre("Single Genre");
        var book = createBook("Single Book", author, List.of(genre));
        createComment("Single Comment", book, LocalDateTime.now());

        // when: job is executed first time
        JobExecution firstExecution = executeJob();
        assertJobCompletedSuccessfully(firstExecution);

        // then: capture state after first execution
        Set<ObjectId> authorIds = captureDocumentIds(mongoAuthors.findAll(), AuthorDocument::getId);
        Set<ObjectId> genreIds = captureDocumentIds(mongoGenres.findAll(), GenreDocument::getId);
        Set<ObjectId> bookIds = captureDocumentIds(mongoBooks.findAll(), BookDocument::getId);
        Set<ObjectId> commentIds = captureDocumentIds(mongoComments.findAll(), CommentDocument::getId);
        long mappingCount = idMappings.count();

        // when: job is executed second time without cleanup
        JobExecution secondExecution = executeJob();
        assertJobCompletedSuccessfully(secondExecution);

        // then: entity counts remain the same
        assertThat(mongoAuthors.count()).isEqualTo(authorIds.size());
        assertThat(mongoGenres.count()).isEqualTo(genreIds.size());
        assertThat(mongoBooks.count()).isEqualTo(bookIds.size());
        assertThat(mongoComments.count()).isEqualTo(commentIds.size());
        assertThat(idMappings.count()).isEqualTo(mappingCount);

        // and: document IDs remain unchanged (no duplicates)
        assertThat(captureDocumentIds(mongoAuthors.findAll(), AuthorDocument::getId)).isEqualTo(authorIds);
        assertThat(captureDocumentIds(mongoGenres.findAll(), GenreDocument::getId)).isEqualTo(genreIds);
        assertThat(captureDocumentIds(mongoBooks.findAll(), BookDocument::getId)).isEqualTo(bookIds);
        assertThat(captureDocumentIds(mongoComments.findAll(), CommentDocument::getId)).isEqualTo(commentIds);
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

    private <T> Set<ObjectId> captureDocumentIds(List<T> documents, java.util.function.Function<T, ObjectId> idExtractor) {
        return documents.stream()
                .map(idExtractor)
                .collect(toSet());
    }
}
