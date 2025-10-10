package ru.otus.hw.monitoring;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component("databaseConnection")
@RequiredArgsConstructor
public class DatabaseConnectionHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
                var metaData = connection.getMetaData();
                return Health.up()
                        .withDetail("database", metaData.getDatabaseProductName())
                        .withDetail("version", metaData.getDatabaseProductVersion())
                        .withDetail("driver", metaData.getDriverName())
                        .withDetail("url", metaData.getURL())
                        .build();
            } else {
                return Health.down()
                        .withDetail("reason", "Connection validation failed")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withException(e)
                    .build();
        }
    }
}
