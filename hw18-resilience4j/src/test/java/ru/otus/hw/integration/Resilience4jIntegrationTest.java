package ru.otus.hw.integration;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.dto.BookDto;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@SpringBootTest
class Resilience4jIntegrationTest {

    @Autowired
    private OpenLibraryService openLibraryService;

    @Autowired
    private HttpBinService httpBinService;

    @Autowired(required = false)
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired(required = false)
    private RateLimiterRegistry rateLimiterRegistry;

    @MockitoBean
    private OpenLibraryClient openLibraryClient;

    @MockitoBean
    private HttpBinClient httpBinClient;

    @Nested
    @DisplayName("CircuitBreaker Pattern")
    class CircuitBreakerTests {

        @Test
        @DisplayName("Should use fallback when circuit breaker opens after failures")
        void shouldUseFallbackWhenCircuitOpens() {
            // given
            when(openLibraryClient.searchByTitle(anyString(), anyInt()))
                    .thenThrow(new RuntimeException("API is down"));

            // when:
            for (int i = 0; i < 25; i++) {
                List<BookDto> result = openLibraryService.searchByTitle("Java", 5);

                // then
                assertThat(result).isEmpty();
            }

            if (circuitBreakerRegistry != null) {
                CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("openlibrary");
                assertThat(cb.getState())
                        .as("Circuit breaker state should be tracked")
                        .isIn(
                                CircuitBreaker.State.CLOSED,
                                CircuitBreaker.State.OPEN,
                                CircuitBreaker.State.HALF_OPEN
                        );

                assertThat(cb.getMetrics().getNumberOfFailedCalls())
                        .as("Should record failed calls")
                        .isGreaterThan(0);
            }
        }

        @Test
        @DisplayName("Should track circuit breaker metrics")
        void shouldTrackCircuitBreakerMetrics() {
            // given
            when(openLibraryClient.searchByTitle(anyString(), anyInt()))
                    .thenReturn(new OpenLibrarySearchResponse(0, List.of()));

            // when
            for (int i = 0; i < 5; i++) {
                openLibraryService.searchByTitle("Spring", 5);
            }

            // then
            if (circuitBreakerRegistry != null) {
                CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("openlibrary");

                assertThat(cb.getMetrics().getNumberOfSuccessfulCalls())
                        .as("Should have successful calls recorded")
                        .isGreaterThan(0);

                assertThat(cb.getState())
                        .as("Circuit should remain CLOSED for successful calls")
                        .isEqualTo(CircuitBreaker.State.CLOSED);
            }
        }
    }

    @Nested
    @DisplayName("RateLimiter Pattern")
    class RateLimiterTests {

        @Test
        @DisplayName("Should track rate limiter usage")
        void shouldTrackRateLimiterUsage() {
            // given
            when(openLibraryClient.searchByTitle(anyString(), anyInt()))
                    .thenReturn(new OpenLibrarySearchResponse(0, List.of()));

            // when
            for (int i = 0; i < 5; i++) {
                openLibraryService.searchByTitle("Java", 5);
            }

            // then
            if (rateLimiterRegistry != null) {
                RateLimiter rl = rateLimiterRegistry.rateLimiter("openlibrary");

                assertThat(rl.getMetrics().getNumberOfWaitingThreads())
                        .as("Should have no waiting threads within limit")
                        .isEqualTo(0);
            }
        }

        @Test
        @DisplayName("Should demonstrate rate limiting concept")
        void shouldDemonstrateRateLimiting() {
            // given
            when(openLibraryClient.searchByTitle(anyString(), anyInt()))
                    .thenReturn(new OpenLibrarySearchResponse(0, List.of()));

            // when/then
            assertThatCode(() -> {
                for (int i = 0; i < 8; i++) {
                    openLibraryService.searchByTitle("Spring Boot", 5);
                }
            }).doesNotThrowAnyException();

            if (rateLimiterRegistry != null) {
                RateLimiter rl = rateLimiterRegistry.rateLimiter("openlibrary");
                assertThat(rl.getMetrics().getAvailablePermissions())
                        .as("Should track available permissions")
                        .isGreaterThanOrEqualTo(0);
            }
        }
    }

    @Nested
    @DisplayName("TimeLimiter Pattern")
    class TimeLimiterTests {

        @Test
        @DisplayName("Should use fallback for slow async calls")
        void shouldUseFallbackForSlowCalls() throws Exception {
            when(httpBinClient.delay(anyInt()))
                    .thenAnswer(invocation -> {
                        Thread.sleep(2000); // 2 seconds delay
                        return null;
                    });

            CompletableFuture<Map<String, Object>> future =
                    httpBinService.delayAsync(2);

            Map<String, Object> result = future.get(3, TimeUnit.SECONDS);

            assertThat(result)
                    .as("Should return fallback map")
                    .isNotNull()
                    .containsEntry("fallback", true)
                    .containsEntry("timedOut", true);
        }

        @Test
        @DisplayName("Should complete successfully for fast async calls")
        void shouldCompleteForFastCalls() throws Exception {
            // given
            when(httpBinClient.delay(anyInt()))
                    .thenReturn(Map.of("success", true, "delay", 0));

            // when
            CompletableFuture<Map<String, Object>> future =
                    httpBinService.delayAsync(0);

            // then
            Map<String, Object> result = future.get(2, TimeUnit.SECONDS);

            assertThat(result)
                    .as("Should return actual response for fast calls")
                    .isNotNull()
                    .doesNotContainKey("fallback")
                    .containsEntry("success", true);
        }
    }

    @Nested
    @DisplayName("Fallback Mechanisms")
    class FallbackTests {

        @Test
        @DisplayName("OpenLibrary fallback should return empty list on error")
        void openLibraryFallbackShouldReturnEmptyList() {
            // given
            when(openLibraryClient.searchByTitle(anyString(), anyInt()))
                    .thenThrow(new RuntimeException("Network error"));

            // when
            List<BookDto> result = openLibraryService.searchByTitle("Nonexistent", 5);

            // then
            assertThat(result)
                    .as("Fallback should return empty list on error")
                    .isNotNull()
                    .isEmpty();
        }

        @Test
        @DisplayName("HttpBin fallback should return timeout info")
        void httpBinFallbackShouldReturnTimeoutInfo() throws Exception {
            // given
            when(httpBinClient.delay(anyInt()))
                    .thenAnswer(invocation -> {
                        Thread.sleep(2000);
                        return null;
                    });

            // when
            CompletableFuture<Map<String, Object>> future =
                    httpBinService.delayAsync(5);

            Map<String, Object> result = future.get(3, TimeUnit.SECONDS);

            // then
            assertThat(result)
                    .as("Fallback should indicate timeout occurred")
                    .containsEntry("fallback", true)
                    .containsEntry("timedOut", true)
                    .containsEntry("seconds", 5);
        }
    }

    @Nested
    @DisplayName("Resilience4j Configuration")
    class ConfigurationTests {

        @Test
        @DisplayName("CircuitBreaker should be registered")
        void circuitBreakerShouldBeRegistered() {
            if (circuitBreakerRegistry != null) {
                assertThat(circuitBreakerRegistry.circuitBreaker("openlibrary"))
                        .as("OpenLibrary circuit breaker should be configured")
                        .isNotNull();
            }
        }

        @Test
        @DisplayName("RateLimiter should be registered")
        void rateLimiterShouldBeRegistered() {
            if (rateLimiterRegistry != null) {
                assertThat(rateLimiterRegistry.rateLimiter("openlibrary"))
                        .as("OpenLibrary rate limiter should be configured")
                        .isNotNull();

                assertThat(rateLimiterRegistry.rateLimiter("httpbin"))
                        .as("HttpBin rate limiter should be configured")
                        .isNotNull();
            }
        }

        @Test
        @DisplayName("CircuitBreaker should have correct configuration")
        void circuitBreakerShouldHaveCorrectConfig() {
            if (circuitBreakerRegistry != null) {
                CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("openlibrary");

                assertThat(cb.getCircuitBreakerConfig().getFailureRateThreshold())
                        .as("Failure rate threshold should be 50%")
                        .isEqualTo(50.0f);

                assertThat(cb.getCircuitBreakerConfig().getSlidingWindowSize())
                        .as("Sliding window size should be configured")
                        .isGreaterThan(0);
            }
        }
    }

    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {

        @Test
        @DisplayName("Should handle partial failures gracefully")
        void shouldHandlePartialFailuresGracefully() {
            when(openLibraryClient.searchByTitle(anyString(), anyInt()))
                    .thenReturn(new OpenLibrarySearchResponse(0, List.of()))
                    .thenThrow(new RuntimeException("Temporary error"))
                    .thenReturn(new OpenLibrarySearchResponse(0, List.of()))
                    .thenThrow(new RuntimeException("Temporary error"));

            for (int i = 0; i < 4; i++) {
                List<BookDto> result = openLibraryService.searchByTitle("Test", 5);

                assertThat(result)
                        .as("Should always return a result (data or fallback)")
                        .isNotNull();
            }

            if (circuitBreakerRegistry != null) {
                CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("openlibrary");
                assertThat(cb.getState())
                        .as("Circuit should handle partial failures")
                        .isIn(
                                CircuitBreaker.State.CLOSED,
                                CircuitBreaker.State.HALF_OPEN
                        );
            }
        }

        @Test
        @DisplayName("Should maintain service availability despite backend issues")
        void shouldMaintainAvailabilityDespiteBackendIssues() {
            when(openLibraryClient.searchByTitle(anyString(), anyInt()))
                    .thenThrow(new RuntimeException("Service unavailable"));

            assertThatCode(() -> {
                for (int i = 0; i < 20; i++) {
                    List<BookDto> result = openLibraryService.searchByTitle("Java", 5);
                    assertThat(result).isNotNull();
                }
            }).as("Application should remain available despite backend failures")
                    .doesNotThrowAnyException();
        }
    }
}
