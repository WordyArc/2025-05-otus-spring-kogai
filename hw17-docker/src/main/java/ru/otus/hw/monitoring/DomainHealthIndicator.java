package ru.otus.hw.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

@Slf4j
@Component("domain")
@RequiredArgsConstructor
public class DomainHealthIndicator implements HealthIndicator {

    private static final int MIN_AUTHORS = 1;

    private static final int MIN_GENRES = 1;

    private static final int MIN_BOOKS = 1;

    private final BookRepository bookRepository;

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    @Override
    @Transactional(readOnly = true)
    public Health health() {
        try {
            return buildDomainHealth(
                    authorRepository.count(),
                    genreRepository.count(),
                    bookRepository.count()
            );
        } catch (Exception e) {
            return domainCheckFailed(e);
        }
    }

    private Health buildDomainHealth(long authors, long genres, long books) {
        boolean hasMinimumAuthors = authors >= MIN_AUTHORS;
        boolean hasMinimumGenres = genres >= MIN_GENRES;
        boolean hasMinimumBooks = books >= MIN_BOOKS;
        boolean isHealthy = hasMinimumAuthors && hasMinimumGenres && hasMinimumBooks;

        var builder = isHealthy ? Health.up() : Health.outOfService();
        return builder
                .withDetail("authors", authors)
                .withDetail("genres", genres)
                .withDetail("books", books)
                .withDetail("status", isHealthy ? "Domain data is present" : "Domain data is missing")
                .build();
    }

    private Health domainCheckFailed(Exception e) {
        log.error("Failed to check domain health", e);
        return Health.down()
                .withDetail("error", "Failed to check domain health")
                .withDetail("message", e.getMessage())
                .withException(e)
                .build();
    }
}
