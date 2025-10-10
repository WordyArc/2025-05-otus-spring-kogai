package ru.otus.hw.monitoring;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;


@Slf4j
@Component
@RequiredArgsConstructor
public class LibraryMetrics implements MeterBinder {

    private final BookRepository bookRepository;

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final CommentRepository commentRepository;

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("library.books.total", bookRepository, repo -> {
            try {
                return repo.count();
            } catch (Exception e) {
                log.error("Failed to get books count", e);
                return 0;
            }
        })
        .description("Total number of books in the library")
        .tag("entity", "book")
        .register(registry);

        Gauge.builder("library.authors.total", authorRepository, repo -> {
            try {
                return repo.count();
            } catch (Exception e) {
                log.error("Failed to get authors count", e);
                return 0;
            }
        })
        .description("Total number of authors in the library")
        .tag("entity", "author")
        .register(registry);

        Gauge.builder("library.genres.total", genreRepository, repo -> {
            try {
                return repo.count();
            } catch (Exception e) {
                log.error("Failed to get genres count", e);
                return 0;
            }
        })
        .description("Total number of genres in the library")
        .tag("entity", "genre")
        .register(registry);

        Gauge.builder("library.comments.total", commentRepository, repo -> {
            try {
                return repo.count();
            } catch (Exception e) {
                log.error("Failed to get comments count", e);
                return 0;
            }
        })
        .description("Total number of comments in the library")
        .tag("entity", "comment")
        .register(registry);
        
        log.info("Library custom metrics registered successfully");
    }
}
