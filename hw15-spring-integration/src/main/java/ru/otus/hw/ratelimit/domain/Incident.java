package ru.otus.hw.ratelimit.domain;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record Incident(
        UUID id,
        String correlationKey,
        int count,
        Instant windowStart,
        Instant windowEnd,
        Set<String> sampleRoutes,
        Map<Integer, Long> statusHistogram
) {}