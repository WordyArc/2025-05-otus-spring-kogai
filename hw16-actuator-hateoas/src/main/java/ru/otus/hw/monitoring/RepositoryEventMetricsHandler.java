package ru.otus.hw.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;


@Slf4j
@Component
@RepositoryEventHandler
@RequiredArgsConstructor
public class RepositoryEventMetricsHandler {

    private final MeterRegistry meterRegistry;

    @HandleAfterCreate
    public void handleBookCreate(Book book) {
        log.debug("Book created via Data REST: id={}, title={}", book.getId(), book.getTitle());
        Counter.builder("library.books.created")
                .description("Number of books created")
                .tag("type", "entity")
                .tag("source", "datarest")
                .register(meterRegistry)
                .increment();
    }

    @HandleAfterSave
    public void handleBookUpdate(Book book) {
        log.debug("Book updated via Data REST: id={}, title={}", book.getId(), book.getTitle());
        Counter.builder("library.books.updated")
                .description("Number of books updated")
                .tag("type", "entity")
                .tag("source", "datarest")
                .register(meterRegistry)
                .increment();
    }

    @HandleAfterDelete
    public void handleBookDelete(Book book) {
        log.debug("Book deleted via Data REST: id={}, title={}", book.getId(), book.getTitle());
        Counter.builder("library.books.deleted")
                .description("Number of books deleted")
                .tag("type", "entity")
                .tag("source", "datarest")
                .register(meterRegistry)
                .increment();
    }

    @HandleAfterCreate
    public void handleCommentCreate(Comment comment) {
        log.debug("Comment created via Data REST: id={}, text={}", comment.getId(), comment.getText());
        Counter.builder("library.comments.created")
                .description("Number of comments created")
                .tag("type", "entity")
                .tag("source", "datarest")
                .register(meterRegistry)
                .increment();
    }

    @HandleAfterSave
    public void handleCommentUpdate(Comment comment) {
        log.debug("Comment updated via Data REST: id={}, text={}", comment.getId(), comment.getText());
        Counter.builder("library.comments.updated")
                .description("Number of comments updated")
                .tag("type", "entity")
                .tag("source", "datarest")
                .register(meterRegistry)
                .increment();
    }

    @HandleAfterDelete
    public void handleCommentDelete(Comment comment) {
        log.debug("Comment deleted via Data REST: id={}, text={}", comment.getId(), comment.getText());
        Counter.builder("library.comments.deleted")
                .description("Number of comments deleted")
                .tag("type", "entity")
                .tag("source", "datarest")
                .register(meterRegistry)
                .increment();
    }
}
