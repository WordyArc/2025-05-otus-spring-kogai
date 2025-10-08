package ru.otus.hw.ratelimit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import ru.otus.hw.ratelimit.domain.Incident;
import ru.otus.hw.ratelimit.domain.LogEvent;
import ru.otus.hw.ratelimit.gateway.LogGateway;
import ru.otus.hw.ratelimit.notify.EmailNotifier;
import ru.otus.hw.ratelimit.notify.JiraNotifier;
import ru.otus.hw.ratelimit.notify.TelegramNotifier;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Import(FixedClockConfig.class)
class DetectionClientIdModeTest {

    private static final int VERIFICATION_TIMEOUT_MS = 500;

    @MockitoSpyBean
    private TelegramNotifier telegram;

    @MockitoSpyBean
    private EmailNotifier email;

    @MockitoSpyBean
    private JiraNotifier jira;

    @MockitoSpyBean
    private LogGateway gateway;

    @Nested
    @DisplayName("Incident triggering")
    class IncidentTriggering {

        @Test
        @DisplayName("Should trigger incident when threshold is reached by client ID")
        void shouldTriggerIncidentWhenThresholdReached() {
            // given: error events within time window
            // Window: [00:00:08Z .. 00:00:10Z], threshold = 3 errors
            gateway.publish(createLogEvent("2025-01-01T00:00:09Z", "client-1", "1.1.1.1", "/api/users", 200));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.100Z", "client-1", "1.1.1.1", "/api/users", 429));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.200Z", "client-1", "1.1.1.1", "/api/orders", 500));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.300Z", "client-1", "1.1.1.1", "/api/users", 429));

            // when: threshold is reached

            // then: incident is triggered and all notifiers are called
            ArgumentCaptor<Incident> incidentCaptor = ArgumentCaptor.forClass(Incident.class);
            verify(telegram, timeout(VERIFICATION_TIMEOUT_MS)).notify(incidentCaptor.capture());
            verify(email, timeout(VERIFICATION_TIMEOUT_MS)).notify(any(Incident.class));
            verify(jira, timeout(VERIFICATION_TIMEOUT_MS)).notify(any(Incident.class));

            // and: incident contains correct data
            Incident incident = incidentCaptor.getValue();
            assertThat(incident.correlationKey()).isEqualTo("client-1");
            assertThat(incident.count()).isEqualTo(3);
            assertThat(incident.sampleRoutes()).containsExactlyInAnyOrder("/api/users", "/api/orders");
            assertThat(incident.statusHistogram())
                    .hasSize(2)
                    .containsEntry(429, 2L)
                    .containsEntry(500, 1L);
        }

        @Test
        @DisplayName("Should aggregate multiple error types in incident")
        void shouldAggregateMultipleErrorTypes() {
            // given
            gateway.publish(createLogEvent("2025-01-01T00:00:09Z", "client-multi", "3.3.3.3", "/api/endpoint1", 429));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.100Z", "client-multi", "3.3.3.3", "/api/endpoint2", 500));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.200Z", "client-multi", "3.3.3.3", "/api/endpoint3", 503));

            // when: threshold is reached

            // then: incident contains all error types
            ArgumentCaptor<Incident> incidentCaptor = ArgumentCaptor.forClass(Incident.class);
            verify(telegram, timeout(VERIFICATION_TIMEOUT_MS)).notify(incidentCaptor.capture());

            Incident incident = incidentCaptor.getValue();
            assertThat(incident.count()).isEqualTo(3);
            assertThat(incident.statusHistogram())
                    .containsEntry(429, 1L)
                    .containsEntry(500, 1L)
                    .containsEntry(503, 1L);
            assertThat(incident.sampleRoutes())
                    .containsExactlyInAnyOrder("/api/endpoint1", "/api/endpoint2", "/api/endpoint3");
        }
    }

    @Nested
    @DisplayName("Threshold validation")
    class ThresholdValidation {

        @Test
        @DisplayName("Should trigger incident exactly at threshold")
        void shouldTriggerIncidentExactlyAtThreshold() {
            // given: exactly 3 error events
            gateway.publish(createLogEvent("2025-01-01T00:00:09Z", "client-exact", "1.1.1.6", "/api/test", 429));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.100Z", "client-exact", "1.1.1.6", "/api/test", 500));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.200Z", "client-exact", "1.1.1.6", "/api/test", 503));

            // when: threshold is exactly reached

            // then: incident is triggered
            ArgumentCaptor<Incident> incidentCaptor = ArgumentCaptor.forClass(Incident.class);
            verify(telegram, timeout(VERIFICATION_TIMEOUT_MS)).notify(incidentCaptor.capture());

            Incident incident = incidentCaptor.getValue();
            assertThat(incident.count()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Status filtering")
    class StatusFiltering {

        @Test
        @DisplayName("Should exclude successful statuses from incident detection")
        void shouldExcludeSuccessfulStatuses() {
            // given: many events but only 1 error (< threshold)
            gateway.publish(createLogEvent("2025-01-01T00:00:09Z", "client-success", "1.1.1.3", "/api/data", 200));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.050Z", "client-success", "1.1.1.3", "/api/data", 201));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.100Z", "client-success", "1.1.1.3", "/api/data", 429));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.150Z", "client-success", "1.1.1.3", "/api/data", 200));

            // when: only 1 problematic status is filtered (< threshold)

            // then: no incident is triggered
            verify(telegram, timeout(400).times(0)).notify(any(Incident.class));
            verify(email, timeout(400).times(0)).notify(any(Incident.class));
            verify(jira, timeout(400).times(0)).notify(any(Incident.class));
        }

        @Test
        @DisplayName("Should include only error statuses in incident count")
        void shouldIncludeOnlyErrorStatuses() {
            // given: mix ¯\_(ツ)_/¯
            gateway.publish(createLogEvent("2025-01-01T00:00:09Z", "client-filter", "1.1.1.7", "/api/test", 200));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.050Z", "client-filter", "1.1.1.7", "/api/test", 429));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.100Z", "client-filter", "1.1.1.7", "/api/test", 201));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.150Z", "client-filter", "1.1.1.7", "/api/test", 500));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.200Z", "client-filter", "1.1.1.7", "/api/test", 204));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.250Z", "client-filter", "1.1.1.7", "/api/test", 503));

            // when: exactly 3 error statuses (429, 500, 503) reach

            // then: incident is triggered with only error statuses
            ArgumentCaptor<Incident> incidentCaptor = ArgumentCaptor.forClass(Incident.class);
            verify(telegram, timeout(VERIFICATION_TIMEOUT_MS)).notify(incidentCaptor.capture());

            Incident incident = incidentCaptor.getValue();
            assertThat(incident.count()).isEqualTo(3);
            assertThat(incident.statusHistogram().keySet())
                    .containsExactlyInAnyOrder(429, 500, 503)
                    .doesNotContain(200, 201, 204);
        }

        @Test
        @DisplayName("Should not filter 404 errors")
        void shouldNotFilter404Errors() {
            // given: 404 errors are NOT included in problem statuses (only 429 and >= 500)
            gateway.publish(createLogEvent("2025-01-01T00:00:09Z", "client-404", "1.1.1.8", "/api/test", 404));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.100Z", "client-404", "1.1.1.8", "/api/test", 404));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.200Z", "client-404", "1.1.1.8", "/api/test", 404));

            // when: 404 events are filtered out (not >= 500, not == 429)

            // then: no incident is triggered
            verify(telegram, timeout(400).times(0)).notify(any(Incident.class));
            verify(email, timeout(400).times(0)).notify(any(Incident.class));
            verify(jira, timeout(400).times(0)).notify(any(Incident.class));
        }
    }

    @Nested
    @DisplayName("Time window handling")
    class TimeWindowHandling {

        @Test
        @DisplayName("Should count only events within time window when threshold is reached")
        void shouldCountOnlyEventsWithinTimeWindow() {
            // given: old events outside window (before 00:00:08Z)
            gateway.publish(createLogEvent("2025-01-01T00:00:07.000Z", "client-window", "1.1.1.8", "/api/test", 429));
            gateway.publish(createLogEvent("2025-01-01T00:00:07.500Z", "client-window", "1.1.1.8", "/api/test", 500));

            // and: fresh events within window that reach threshold
            gateway.publish(createLogEvent("2025-01-01T00:00:09.000Z", "client-window", "1.1.1.8", "/api/test", 429));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.050Z", "client-window", "1.1.1.8", "/api/test", 500));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.100Z", "client-window", "1.1.1.8", "/api/other", 500));

            // when: threshold is reached with fresh events

            // then: incident contains only fresh events
            ArgumentCaptor<Incident> incidentCaptor = ArgumentCaptor.forClass(Incident.class);
            verify(telegram, timeout(VERIFICATION_TIMEOUT_MS)).notify(incidentCaptor.capture());

            Incident incident = incidentCaptor.getValue();
            assertThat(incident.correlationKey()).isEqualTo("client-window");
            assertThat(incident.count()).isEqualTo(3);
            assertThat(incident.sampleRoutes()).containsExactlyInAnyOrder("/api/test", "/api/other");
        }

        @Test
        @DisplayName("Should process events at window boundaries correctly")
        void shouldProcessEventsAtWindowBoundaries() {
            // given: events at exact window boundaries
            // Window: [00:00:08Z .. 00:00:10Z]
            gateway.publish(createLogEvent("2025-01-01T00:00:08.000Z", "client-boundary", "1.1.1.9", "/api/start", 429));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.000Z", "client-boundary", "1.1.1.9", "/api/middle", 500));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.999Z", "client-boundary", "1.1.1.9", "/api/end", 503));

            // when: threshold is reached with boundary events

            // then: incident is triggered
            ArgumentCaptor<Incident> incidentCaptor = ArgumentCaptor.forClass(Incident.class);
            verify(telegram, timeout(VERIFICATION_TIMEOUT_MS)).notify(incidentCaptor.capture());

            Incident incident = incidentCaptor.getValue();
            assertThat(incident.count()).isEqualTo(3);
        }
    }

    private LogEvent createLogEvent(String timestamp, String clientId, String ip, String route, int status) {
        return new LogEvent(Instant.parse(timestamp), clientId, ip, route, status);
    }
}
