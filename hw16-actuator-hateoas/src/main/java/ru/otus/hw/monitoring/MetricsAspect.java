package ru.otus.hw.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Aspect for monitoring service layer operations.
 * Note: This aspect monitors traditional service layer calls.
 * For Spring Data REST operations, see {@link RepositoryEventMetricsHandler}.
 */
@Aspect
@Component
public class MetricsAspect {

    private final MeterRegistry registry;

    private final Counter booksCreated;

    private final Counter commentsCreated;

    private final AtomicInteger activeBookOps;

    public MetricsAspect(MeterRegistry registry) {
        this.registry = registry;
        this.booksCreated = Counter.builder("library.books.created")
                .description("Number of books created via service layer")
                .tag("type", "entity")
                .tag("source", "service")
                .register(registry);
        this.commentsCreated = Counter.builder("library.comments.created")
                .description("Number of comments created via service layer")
                .tag("type", "entity")
                .tag("source", "service")
                .register(registry);
        this.activeBookOps = registry.gauge("library.books.active.operations",
                new AtomicInteger(0));
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

    @SneakyThrows
    @Around("execution(* ru.otus.hw.services.BookService.*(..))")
    public Object timeBookOps(ProceedingJoinPoint pjp) {
        final String method = pjp.getSignature().getName();
        activeBookOps.incrementAndGet();
        final Timer timer = Timer.builder("library.books.operation.duration")
                .description("Time spent in book operations")
                .tag("method", method)
                .tag("source", "service")
                .publishPercentileHistogram()
                .register(registry);
        
        Timer.Sample sample = Timer.start(registry);
        try {
            return pjp.proceed();
        } finally {
            sample.stop(timer);
            activeBookOps.decrementAndGet();
        }
    }
}
