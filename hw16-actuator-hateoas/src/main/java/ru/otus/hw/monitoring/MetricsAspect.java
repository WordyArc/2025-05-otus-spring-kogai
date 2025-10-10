package ru.otus.hw.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.stereotype.Component;

@Aspect
@Component
public class MetricsAspect {

    private final MeterRegistry registry;

    private final Counter booksCreated;

    private final Counter commentsCreated;

    public MetricsAspect(MeterRegistry registry) {
        this.registry = registry;
        this.booksCreated = Counter.builder("library.books.created")
                .description("Number of books created")
                .register(registry);
        this.commentsCreated = Counter.builder("library.comments.created")
                .description("Number of comments created")
                .register(registry);
    }

    @AfterReturning("execution(* ru.otus.hw.services.BookService.insert(..))")
    public void onBookCreated() {
        booksCreated.increment();
    }

    @AfterReturning("execution(* ru.otus.hw.services.CommentService.create(..))")
    public void onCommentCreated() {
        commentsCreated.increment();
    }

    @AfterThrowing(pointcut = "within(ru.otus.hw.services..*)", throwing = "ex")
    public void onServiceError(Exception ex) {
        Counter.builder("library.service.errors")
                .description("Service layer errors")
                .tag("exception", ex.getClass().getSimpleName())
                .register(registry)
                .increment();
    }
}
