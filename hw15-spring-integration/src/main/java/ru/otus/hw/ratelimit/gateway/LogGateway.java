package ru.otus.hw.ratelimit.gateway;


import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.handler.annotation.Payload;
import ru.otus.hw.ratelimit.domain.LogEvent;

@MessagingGateway(defaultRequestChannel = "detectionFlow.input")
public interface LogGateway {
    void publish(@Payload LogEvent log);
}
