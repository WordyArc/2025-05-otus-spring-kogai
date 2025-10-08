package ru.otus.hw.ratelimit.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record LogEvent(
        @NotNull Instant timestamp,
        @NotBlank String clientId,
        @NotBlank String ip,
        @NotBlank String route,
        int status
) {
}
