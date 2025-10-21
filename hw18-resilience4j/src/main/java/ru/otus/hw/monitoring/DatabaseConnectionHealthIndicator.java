package ru.otus.hw.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@Component("databaseConnection")
@RequiredArgsConstructor
public class DatabaseConnectionHealthIndicator implements HealthIndicator {

    private static final int CONNECTION_TIMEOUT_SECONDS = 2;

    private final DataSource dataSource;

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(CONNECTION_TIMEOUT_SECONDS)
                    ? healthyConnection(connection)
                    : unhealthyConnection();
        } catch (Exception e) {
            return failedToConnect(e);
        }
    }

    private Health healthyConnection(Connection connection) throws SQLException {
        var metaData = connection.getMetaData();
        return Health.up()
                .withDetail("database", metaData.getDatabaseProductName())
                .withDetail("version", metaData.getDatabaseProductVersion())
                .withDetail("driver", metaData.getDriverName())
                .withDetail("driverVersion", metaData.getDriverVersion())
                .withDetail("url", maskingUrl(metaData.getURL()))
                .withDetail("username", metaData.getUserName())
                .withDetail("validationTimeout", CONNECTION_TIMEOUT_SECONDS + "s")
                .build();
    }

    private Health unhealthyConnection() {
        log.warn("Database connection validation failed");
        return Health.down()
                .withDetail("reason", "Connection validation failed")
                .withDetail("timeout", CONNECTION_TIMEOUT_SECONDS + "s")
                .build();
    }

    private Health failedToConnect(Exception e) {
        log.error("Failed to check database connection health", e);
        return Health.down()
                .withDetail("error", "Failed to establish database connection")
                .withDetail("message", e.getMessage())
                .withException(e)
                .build();
    }

    private String maskingUrl(String url) {
        if (url == null) {
            return "N/A";
        }
        return url.replaceAll("password=[^;&]*", "password=***");
    }
}
