package ru.otus.hw;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import ru.otus.hw.config.MongoTestConfig;

import java.util.UUID;


@Import(MongoTestConfig.class)
public abstract class CommonContext {

    @DynamicPropertySource
    static void overrideMongoProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.database", CommonContext::randomDbName);
    }

    private static String randomDbName() {
        return "test_" + UUID.randomUUID().toString().replace("-", "");
    }
}
