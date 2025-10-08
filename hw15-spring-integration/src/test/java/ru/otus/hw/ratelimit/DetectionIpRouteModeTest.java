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

@SpringBootTest(properties = {
        "detector.threshold=3",
        "detector.window=PT2S",
        "detector.correlation-mode=IP_ROUTE"
})
@Import(FixedClockConfig.class)
class DetectionIpRouteModeTest {

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
    @DisplayName("Incident triggering by IP and Route")
    class IncidentTriggeringByIpRoute {

        @Test
        @DisplayName("Should trigger incident when threshold is reached for specific IP and route combination")
        void shouldTriggerIncidentForIpAndRoute() {
            // given: same IP address and route with threshold errors
            // Window: [00:00:08Z .. 00:00:10Z], threshold = 3, mode = IP_ROUTE
            gateway.publish(createLogEvent("2025-01-01T00:00:09Z", "client-1", "10.0.0.1", "/api/payment", 429));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.050Z", "client-1", "10.0.0.1", "/api/payment", 500));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.100Z", "client-1", "10.0.0.1", "/api/payment", 429));

            // when: threshold is reached for IP|route combination

            // then: incident is triggered with IP|route correlation key
            ArgumentCaptor<Incident> incidentCaptor = ArgumentCaptor.forClass(Incident.class);
            verify(telegram, timeout(VERIFICATION_TIMEOUT_MS)).notify(incidentCaptor.capture());
            verify(email, timeout(VERIFICATION_TIMEOUT_MS)).notify(any(Incident.class));
            verify(jira, timeout(VERIFICATION_TIMEOUT_MS)).notify(any(Incident.class));

            // and: incident contains correct IP|route correlation key
            Incident incident = incidentCaptor.getValue();
            assertThat(incident.correlationKey()).isEqualTo("10.0.0.1|/api/payment");
            assertThat(incident.count()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should aggregate error types for same IP and route")
        void shouldAggregateErrorTypesForIpRoute() {
            // given: multiple error types IP|route
            gateway.publish(createLogEvent("2025-01-01T00:00:09Z", "client-2", "10.0.0.2", "/api/checkout", 429));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.050Z", "client-2", "10.0.0.2", "/api/checkout", 500));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.100Z", "client-2", "10.0.0.2", "/api/checkout", 503));

            // when: threshold is reached

            // then
            ArgumentCaptor<Incident> incidentCaptor = ArgumentCaptor.forClass(Incident.class);
            verify(telegram, timeout(VERIFICATION_TIMEOUT_MS)).notify(incidentCaptor.capture());

            Incident incident = incidentCaptor.getValue();
            assertThat(incident.correlationKey()).isEqualTo("10.0.0.2|/api/checkout");
            assertThat(incident.count()).isEqualTo(3);
            assertThat(incident.statusHistogram())
                    .containsEntry(429, 1L)
                    .containsEntry(500, 1L)
                    .containsEntry(503, 1L);
        }

        @Test
        @DisplayName("Should track multiple routes for same IP independently")
        void shouldTrackMultipleRoutesIndependently() {
            // given: same IP but different routes, each reaching threshold
            // route 1
            gateway.publish(createLogEvent("2025-01-01T00:00:09Z", "client-3", "10.0.0.3", "/api/orders", 429));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.050Z", "client-3", "10.0.0.3", "/api/orders", 500));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.100Z", "client-3", "10.0.0.3", "/api/orders", 503));

            // route 2: /api/cart - noise, should not affect /api/orders
            gateway.publish(createLogEvent("2025-01-01T00:00:09.150Z", "client-3", "10.0.0.3", "/api/cart", 429));

            // when: first route reaches threshold

            // then: incident is triggered only for /api/orders route
            ArgumentCaptor<Incident> incidentCaptor = ArgumentCaptor.forClass(Incident.class);
            verify(telegram, timeout(VERIFICATION_TIMEOUT_MS)).notify(incidentCaptor.capture());

            Incident incident = incidentCaptor.getValue();
            assertThat(incident.correlationKey()).isEqualTo("10.0.0.3|/api/orders");
            assertThat(incident.count()).isEqualTo(3);
            assertThat(incident.sampleRoutes()).containsExactly("/api/orders");
        }
    }

    @Nested
    @DisplayName("Route isolation")
    class RouteIsolation {

        @Test
        @DisplayName("Should not accumulate errors across different routes")
        void shouldNotAccumulateAcrossDifferentRoutes() {
            // given: same IP but different routes, each below threshold
            gateway.publish(createLogEvent("2025-01-01T00:00:09Z", "client-4", "10.0.0.4", "/api/route-a", 429));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.050Z", "client-4", "10.0.0.4", "/api/route-b", 429));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.100Z", "client-4", "10.0.0.4", "/api/route-c", 429));

            // when: each route has only 1 error (< threshold)

            // then: no incident is triggered
            verify(telegram, timeout(400).times(0)).notify(any(Incident.class));
            verify(email, timeout(400).times(0)).notify(any(Incident.class));
            verify(jira, timeout(400).times(0)).notify(any(Incident.class));
        }

        @Test
        @DisplayName("Should not accumulate errors across different IPs for same route")
        void shouldNotAccumulateAcrossDifferentIps() {
            // given: same route but different IPs, each below threshold
            gateway.publish(createLogEvent("2025-01-01T00:00:09Z", "client-5", "10.0.0.5", "/api/shared", 429));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.050Z", "client-6", "10.0.0.6", "/api/shared", 500));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.100Z", "client-7", "10.0.0.7", "/api/shared", 503));

            // when: each IP has only 1 error (< threshold)

            // then: no incident is triggered
            verify(telegram, timeout(400).times(0)).notify(any(Incident.class));
            verify(email, timeout(400).times(0)).notify(any(Incident.class));
            verify(jira, timeout(400).times(0)).notify(any(Incident.class));
        }

        @Test
        @DisplayName("Should trigger separate incidents for different routes of same IP")
        void shouldTriggerSeparateIncidentsForDifferentRoutes() {
            // given: same IP, two different routes, both reaching threshold
            // route 1: /api/route-x
            gateway.publish(createLogEvent("2025-01-01T00:00:09Z", "client-8", "10.0.0.8", "/api/route-x", 429));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.050Z", "client-8", "10.0.0.8", "/api/route-x", 500));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.100Z", "client-8", "10.0.0.8", "/api/route-x", 503));

            // route 2: /api/route-y
            gateway.publish(createLogEvent("2025-01-01T00:00:09.150Z", "client-8", "10.0.0.8", "/api/route-y", 429));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.200Z", "client-8", "10.0.0.8", "/api/route-y", 500));
            gateway.publish(createLogEvent("2025-01-01T00:00:09.250Z", "client-8", "10.0.0.8", "/api/route-y", 503));

            // when: both routes reach threshold

            // then: two separate incidents are triggered
            ArgumentCaptor<Incident> incidentCaptor = ArgumentCaptor.forClass(Incident.class);
            verify(telegram, timeout(VERIFICATION_TIMEOUT_MS).times(2)).notify(incidentCaptor.capture());

            // and: each incident has different correlation key
            var incidents = incidentCaptor.getAllValues();
            assertThat(incidents).hasSize(2);
            assertThat(incidents)
                    .extracting(Incident::correlationKey)
                    .containsExactlyInAnyOrder("10.0.0.8|/api/route-x", "10.0.0.8|/api/route-y");
        }
    }

    private LogEvent createLogEvent(String timestamp, String clientId, String ip, String route, int status) {
        return new LogEvent(Instant.parse(timestamp), clientId, ip, route, status);
    }
}