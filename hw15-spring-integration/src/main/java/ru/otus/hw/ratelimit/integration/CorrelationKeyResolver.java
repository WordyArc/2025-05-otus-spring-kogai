package ru.otus.hw.ratelimit.integration;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.ratelimit.config.DetectorProperties;
import ru.otus.hw.ratelimit.domain.LogEvent;

@Component
@RequiredArgsConstructor
public class CorrelationKeyResolver {
    private final DetectorProperties props;

    public String resolve(LogEvent e) {
        return switch (props.correlationMode()) {
            case CLIENT_ID -> e.clientId();
            case IP_ROUTE  -> e.ip() + "|" + e.route();
        };
    }
}
