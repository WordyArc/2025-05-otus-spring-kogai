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

import java.util.function.Supplier;


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
        registerLibraryMetrics(registry);
        log.info("Library custom metrics registered successfully");
    }

    private void registerLibraryMetrics(MeterRegistry registry) {
        registerCountGauge(registry, "library.books.total", "book",
                "Total number of books in the library", bookRepository::count);
        registerCountGauge(registry, "library.authors.total", "author",
                "Total number of authors in the library", authorRepository::count);
        registerCountGauge(registry, "library.genres.total", "genre",
                "Total number of genres in the library", genreRepository::count);
        registerCountGauge(registry, "library.comments.total", "comment",
                "Total number of comments in the library", commentRepository::count);
    }

    private void registerCountGauge(
            MeterRegistry registry,
            String metricName,
            String entity,
            String description,
            Supplier<Long> countSupplier
    ) {
        Gauge.builder(metricName, () -> safeCount(countSupplier, entity))
                .description(description)
                .tag("entity", entity)
                .register(registry);
    }

    private double safeCount(Supplier<Long> countSupplier, String entity) {
        try {
            return countSupplier.get();
        } catch (Exception e) {
            log.error("Failed to get {} count", entity, e);
            return 0;
        }
    }
}
