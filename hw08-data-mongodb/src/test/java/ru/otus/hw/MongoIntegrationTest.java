package ru.otus.hw;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

@Testcontainers(disabledWithoutDocker = true)
public abstract class MongoIntegrationTest {

    private static final DockerImageName MONGO_IMAGE =
            DockerImageName.parse("mongo:7.0.14");

    @ServiceConnection
    protected static final MongoDBContainer MONGO = new MongoDBContainer(MONGO_IMAGE)
            .withReuse(true);

    @DynamicPropertySource
    static void overrideMongoProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.database", MongoIntegrationTest::randomDbName);
    }

    private static String randomDbName() {
        return "test_" + UUID.randomUUID().toString().replace("-", "");
    }
}
