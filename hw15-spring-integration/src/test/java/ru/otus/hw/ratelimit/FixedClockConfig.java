package ru.otus.hw.ratelimit;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

@TestConfiguration
public class FixedClockConfig {
    @Bean
    @Primary
    public Clock testClock() {
        return Clock.fixed(Instant.parse("2025-01-01T00:00:10Z"), ZoneOffset.UTC);
    }
}
