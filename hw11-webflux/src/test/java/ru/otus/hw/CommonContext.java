package ru.otus.hw;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import ru.otus.hw.config.TestDataConfig;

import java.util.UUID;

@Import(TestDataConfig.class)
public abstract class CommonContext {

    @DynamicPropertySource
    static void overrideDbProps(DynamicPropertyRegistry registry) {
        var db = randomDbName();

        registry.add("spring.datasource.url", () -> jdbcUrl(db));
        registry.add("spring.r2dbc.url", () -> r2dbcUrl(db));
        registry.add("spring.liquibase.url", () -> jdbcUrl(db));
    }

    private static String randomDbName() {
        return "test_" + UUID.randomUUID().toString().replace("-", "");
    }

    private static String jdbcUrl(String db) {
        return "jdbc:h2:mem:" + db + ";DB_CLOSE_DELAY=-1";
    }

    private static String r2dbcUrl(String db) {
        return "r2dbc:h2:mem:///" + db + "?options=DB_CLOSE_DELAY=-1";
    }
}
