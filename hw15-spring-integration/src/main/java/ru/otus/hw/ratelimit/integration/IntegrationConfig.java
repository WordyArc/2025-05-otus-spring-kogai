package ru.otus.hw.ratelimit.integration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.store.MessageGroupStore;
import org.springframework.integration.store.SimpleMessageStore;
import ru.otus.hw.ratelimit.config.DetectorProperties;
import ru.otus.hw.ratelimit.domain.LogEvent;
import ru.otus.hw.ratelimit.notify.EmailNotifier;
import ru.otus.hw.ratelimit.notify.JiraNotifier;
import ru.otus.hw.ratelimit.notify.TelegramNotifier;

import java.time.Clock;

@Configuration
public class IntegrationConfig {

    public static final String HDR_CORR = "correlationKey";

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    MessageGroupStore messageGroupStore() {
        return new SimpleMessageStore();
    }

    @Bean
    IntegrationFlow detectionFlow(CorrelationKeyResolver keyResolver,
                                  DetectorProperties props,
                                  IncidentFactory incidentFactory,
                                  MessageGroupStore store,
                                  Clock clock
    ) {
        return flow -> flow
                .log(LoggingHandler.Level.DEBUG, message -> "LogEvent in: " + message.getPayload())
                .enrichHeaders(spec -> spec.headerFunction(
                        HDR_CORR,
                        message -> keyResolver.resolve((LogEvent) message.getPayload()))
                )
                .aggregate(a -> a
                        .messageStore(store)
                        .correlationStrategy(message -> message.getHeaders().get(HDR_CORR))
                        .releaseStrategy(new SlidingWindowReleaseStrategy(props, clock))
                        .groupTimeout(props.window().toMillis())
                        .expireGroupsUponCompletion(true)
                        .expireGroupsUponTimeout(true)
                        .sendPartialResultOnExpiry(false)
                        .outputProcessor(incidentFactory::fromGroup)
                )
                .log(LoggingHandler.Level.DEBUG, message -> "Incident out: " + message.getPayload())
                .channel("notifyRouter.input");
    }

    @Bean
    IntegrationFlow errorFlow() {
        return IntegrationFlow
                .from(IntegrationContextUtils.ERROR_CHANNEL_BEAN_NAME)
                .log(LoggingHandler.Level.ERROR, m -> "ERROR in flow: " + m.getPayload())
                .get();
    }

    @Bean
    IntegrationFlow notifyRouter(TelegramNotifier telegram, EmailNotifier email, JiraNotifier jira) {
        return f -> f
                .bridge()
                .routeToRecipients(r -> r
                        .recipient("telegramFlow.input")
                        .recipient("emailFlow.input")
                        .recipient("jiraFlow.input"));
    }

    @Bean
    IntegrationFlow telegramFlow(TelegramNotifier h) {
        return f -> f.handle(h, "notify");
    }

    @Bean
    IntegrationFlow emailFlow(EmailNotifier h) {
        return f -> f.handle(h, "notify");
    }

    @Bean
    IntegrationFlow jiraFlow(JiraNotifier h) {
        return f -> f.handle(h, "notify");
    }

}
