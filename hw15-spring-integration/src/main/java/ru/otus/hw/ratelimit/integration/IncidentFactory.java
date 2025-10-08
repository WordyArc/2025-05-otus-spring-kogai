package ru.otus.hw.ratelimit.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.integration.store.MessageGroup;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import ru.otus.hw.ratelimit.config.DetectorProperties;
import ru.otus.hw.ratelimit.domain.Incident;
import ru.otus.hw.ratelimit.domain.LogEvent;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class IncidentFactory {
    private final DetectorProperties props;

    private final Clock clock;

    public Incident fromGroup(MessageGroup group) {
        Instant now = clock.instant();

        Instant windowStart = now.minus(props.window());

        List<LogEvent> recent = group.getMessages().stream()
                .map(Message::getPayload)
                .map(LogEvent.class::cast)
                .filter(e -> !e.timestamp().isBefore(windowStart))
                .toList();

        var routes = recent.stream().map(LogEvent::route).collect(Collectors.toSet());
        var histogram = recent.stream().collect(Collectors.groupingBy(LogEvent::status, Collectors.counting()));
        String correlationKey = String.valueOf(group.getGroupId());

        return new Incident(
                UUID.randomUUID(),
                correlationKey,
                recent.size(),
                windowStart,
                now,
                routes,
                histogram
        );
    }
}
