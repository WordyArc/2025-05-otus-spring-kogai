package ru.otus.hw.ratelimit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.otus.hw.ratelimit.domain.CorrelationMode;

import java.time.Duration;

@ConfigurationProperties(prefix = "detector")
public record DetectorProperties(
        int threshold,

        Duration window,

        CorrelationMode correlationMode
) {
}
