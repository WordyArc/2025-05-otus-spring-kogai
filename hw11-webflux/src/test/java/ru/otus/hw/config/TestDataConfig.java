package ru.otus.hw.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import ru.otus.hw.TestData;

@TestConfiguration
public class TestDataConfig {

    @Bean
    @DependsOn("liquibase")
    public TestData testData(R2dbcEntityOperations operations) {
        return new TestData(operations);
    }
}
