package ru.otus.hw.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.function.Consumer;

@Configuration
public class Resilience4jConfig {


    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
        CircuitBreakerConfig cbDefault = CircuitBreakerConfig.custom()
                .failureRateThreshold(50f)
                .slowCallRateThreshold(50f)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .minimumNumberOfCalls(10)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(20)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(5)
                .build();

        TimeLimiterConfig tlDefault = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(3))
                .cancelRunningFuture(true)
                .build();

        CircuitBreakerConfig cbOpenLib = CircuitBreakerConfig
                .from(cbDefault)
                .build();

        TimeLimiterConfig tlOpenLib = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5))
                .build();

        CircuitBreakerConfig cbHttpBin = CircuitBreakerConfig
                .from(cbDefault)
                .slowCallDurationThreshold(Duration.ofSeconds(1))
                .build();

        TimeLimiterConfig tlHttpBin = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(1))
                .build();

        return factory -> {
            factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                    .circuitBreakerConfig(cbDefault)
                    .timeLimiterConfig(tlDefault)
                    .build());

            factory.configure(cfg(cbOpenLib, tlOpenLib), "openlibrary");

            factory.configure(cfg(cbHttpBin, tlHttpBin), "httpbin");
            factory.configure(cfg(cbHttpBin, tlHttpBin), "httpbin-slow");
            factory.configure(cfg(cbHttpBin, tlHttpBin), "httpbin-status");
        };
    }

    private static Consumer<Resilience4JConfigBuilder> cfg(
            CircuitBreakerConfig cb, TimeLimiterConfig tl) {
        return builder -> builder
                .circuitBreakerConfig(cb)
                .timeLimiterConfig(tl)
                .build();
    }
}