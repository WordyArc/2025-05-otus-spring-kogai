package ru.otus.hw.integration;


import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class HttpBinService {

    private final HttpBinClient httpBinClient;

    private final TaskExecutor resilienceExecutor;

    @TimeLimiter(name = "httpbin", fallbackMethod = "delayAsyncFallback")
    @RateLimiter(name = "httpbin")
    public CompletableFuture<Map<String, Object>> delayAsync(int seconds) {
        return CompletableFuture.supplyAsync(() -> httpBinClient.delay(seconds), resilienceExecutor::execute);
    }

    private CompletableFuture<Map<String, Object>> delayAsyncFallback(int seconds, Throwable t) {
        log.warn("TimeLimiter fallback for httpbin.delay ({}s): {}", seconds, t == null ? "unknown" : t.toString());
        return CompletableFuture.completedFuture(
                Map.of("fallback", true, "timedOut", true, "seconds", seconds)
        );
    }
}
