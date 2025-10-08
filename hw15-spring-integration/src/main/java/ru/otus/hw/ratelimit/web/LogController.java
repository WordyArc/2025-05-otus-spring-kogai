package ru.otus.hw.ratelimit.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.ratelimit.domain.LogEvent;
import ru.otus.hw.ratelimit.gateway.LogGateway;

import java.time.Clock;
import java.time.Instant;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogGateway gateway;

    private final Clock clock;

    public record LogEventDto(String timestamp,
                              @NotBlank String clientId,
                              @NotBlank String ip,
                              @NotBlank String route,
                              @Min(100) @Max(599) int status) {
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void accept(@Valid @RequestBody LogEventDto dto) {
        var ts = (dto.timestamp() == null || dto.timestamp().isBlank())
                ? Instant.now(clock)
                : Instant.parse(dto.timestamp());
        gateway.publish(new LogEvent(ts, dto.clientId(), dto.ip(), dto.route(), dto.status()));
    }
}
