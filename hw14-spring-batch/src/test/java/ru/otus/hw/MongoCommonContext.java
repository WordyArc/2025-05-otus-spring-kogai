package ru.otus.hw;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;


public abstract class MongoCommonContext {

    @DynamicPropertySource
    static void overrideMongoProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.database", MongoCommonContext::randomDbName);
    }

    private static String randomDbName() {
        return "test_" + UUID.randomUUID().toString().replace("-", "");
    }
}
