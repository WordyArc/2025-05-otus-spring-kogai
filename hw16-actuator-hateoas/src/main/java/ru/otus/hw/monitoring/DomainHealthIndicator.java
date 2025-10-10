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

            var builder = (authors > 0 && genres > 0 && books > 0) ? Health.up() : Health.outOfService();
            return builder
                    .withDetail("authors", authors)
                    .withDetail("genres", genres)
                    .withDetail("books", books)
                    .build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
