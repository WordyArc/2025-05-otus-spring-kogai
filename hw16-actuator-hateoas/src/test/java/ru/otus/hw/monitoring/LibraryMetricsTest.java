package ru.otus.hw.monitoring;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
class LibraryMetricsTest {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private CommentRepository commentRepository;

    private Author author;
    private Genre genre;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        genreRepository.deleteAll();

        author = createAuthor("Test Author");
        genre = createGenre("Test Genre");
    }

    @Nested
    @DisplayName("Gauge Metrics")
    class GaugeMetrics {

        @Test
        @DisplayName("should track total books count via gauge")
        void shouldTrackBooksCount() {
            // When
            createBook("Book 1");
            createBook("Book 2");

            // Then
            Gauge gauge = findGauge("library.books.total");

            assertThat(gauge).isNotNull();
            assertThat(gauge.value()).isEqualTo(2.0);
        }

        @Test
        @DisplayName("should track total authors count via gauge")
        void shouldTrackAuthorsCount() {
            // When
            createAuthor("Another Author");

            // Then
            Gauge gauge = findGauge("library.authors.total");

            assertThat(gauge).isNotNull();
            assertThat(gauge.value()).isEqualTo(2.0);
        }

        @Test
        @DisplayName("should track total genres count via gauge")
        void shouldTrackGenresCount() {
            // When
            createGenre("Another Genre");

            // Then
            Gauge gauge = findGauge("library.genres.total");

            assertThat(gauge).isNotNull();
            assertThat(gauge.value()).isEqualTo(2.0);
        }

        @Test
        @DisplayName("should track total comments count via gauge")
        void shouldTrackCommentsCount() {
            // When
            var book = createBook("Book");
            createComment("Comment 1", book);
            createComment("Comment 2", book);

            // Then
            Gauge gauge = findGauge("library.comments.total");

            assertThat(gauge).isNotNull();
            assertThat(gauge.value()).isEqualTo(2.0);
        }

        @Test
        @DisplayName("should update gauge value when books are deleted")
        void shouldUpdateGaugeOnDelete() {
            // Given
            var book1 = createBook("Book 1");
            createBook("Book 2");

            Gauge gauge = findGauge("library.books.total");
            assertThat(gauge.value()).isEqualTo(2.0);

            // When
            deleteBook(book1);

            // Then
            assertThat(gauge.value()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("should start with correct initial counts")
        void shouldStartWithCorrectCounts() {
            // Given

            // Then
            assertGaugeValue("library.books.total", 0.0);
            assertGaugeValue("library.authors.total", 1.0);
            assertGaugeValue("library.genres.total", 1.0);
            assertGaugeValue("library.comments.total", 0.0);
        }
    }

    @Nested
    @DisplayName("Metric Availability")
    class MetricAvailability {

        @Test
        @DisplayName("should have all gauge metrics registered")
        void shouldHaveAllGaugeMetrics() {
            assertThat(findGauge("library.books.total")).isNotNull();
            assertThat(findGauge("library.authors.total")).isNotNull();
            assertThat(findGauge("library.genres.total")).isNotNull();
            assertThat(findGauge("library.comments.total")).isNotNull();
        }

        @Test
        @DisplayName("should have metrics bean properly initialized")
        void shouldHaveMetricsBeanInitialized() {
            assertThat(meterRegistry.getMeters()).isNotEmpty();
            assertThat(meterRegistry.getMeters().stream()
                    .anyMatch(m -> m.getId().getName().startsWith("library."))).isTrue();
        }
    }


    private Author createAuthor(String fullName) {
        return authorRepository.save(new Author(null, fullName));
    }

    private Genre createGenre(String name) {
        return genreRepository.save(new Genre(null, name));
    }

    private Book createBook(String title) {
        var book = bookRepository.save(new Book(null, title, author, new ArrayList<>(List.of(genre))));
        bookRepository.flush();
        return book;
    }

    private Comment createComment(String text, Book book) {
        var comment = commentRepository.save(new Comment(null, text, book, LocalDateTime.now()));
        commentRepository.flush();
        return comment;
    }

    private void deleteBook(Book book) {
        bookRepository.delete(book);
        bookRepository.flush();
    }

    private Gauge findGauge(String metricName) {
        return meterRegistry.find(metricName).gauge();
    }

    private void assertGaugeValue(String metricName, double expectedValue) {
        Gauge gauge = findGauge(metricName);
        assertThat(gauge).isNotNull();
        assertThat(gauge.value()).isEqualTo(expectedValue);
    }
}
