package ru.otus.hw.ratelimit.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.integration.aggregator.ReleaseStrategy;
import org.springframework.integration.store.MessageGroup;
import org.springframework.messaging.Message;
import ru.otus.hw.ratelimit.config.DetectorProperties;
import ru.otus.hw.ratelimit.domain.LogEvent;

import java.time.Clock;
import java.time.Instant;

/**
 * Считаем по скользящему окну [now - window, now] на каждом входящем сообщении.
 * Если count >= threshold — агрегатор «выпускает» группу (т.е. дальше один стреляем Incident)
 * */
@RequiredArgsConstructor
public class SlidingWindowReleaseStrategy implements ReleaseStrategy {
    private final DetectorProperties props;

    private final Clock clock;

    @Override
    public boolean canRelease(MessageGroup group) {
        Instant now = clock.instant();
        Instant windowStart = now.minus(props.window());
        long recent = group.getMessages().stream()
                .map(Message::getPayload)
                .map(LogEvent.class::cast)
                .filter(event -> !event.timestamp().isBefore(windowStart))
                .count();
        return recent >= props.threshold();
    }
}
