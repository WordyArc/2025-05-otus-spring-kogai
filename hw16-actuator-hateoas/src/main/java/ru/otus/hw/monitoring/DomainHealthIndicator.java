package ru.otus.hw.monitoring;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

@Component("domain")
@RequiredArgsConstructor
public class DomainHealthIndicator implements HealthIndicator {

    private final BookRepository bookRepository;

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    @Override
    @Transactional(readOnly = true)
    public Health health() {
        try {
            long books = bookRepository.count();
            long authors = authorRepository.count();
            long genres = genreRepository.count();

            boolean isHealthy = authors > 0 && genres > 0 && books > 0;
            var builder = isHealthy ? Health.up() : Health.outOfService();
            
            return builder
                    .withDetail("authors", authors)
                    .withDetail("genres", genres)
                    .withDetail("books", books)
                    .withDetail("status", isHealthy ? "Domain data is present" : "Domain data is missing")
                    .withDetail("minRequiredAuthors", 1)
                    .withDetail("minRequiredGenres", 1)
                    .withDetail("minRequiredBooks", 1)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", "Failed to check domain health")
                    .withException(e)
                    .build();
        }
    }
}
