package ru.otus.hw.config;

import de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;


@TestConfiguration(proxyBeanMethods = false)
@Import({
        MongoTestConfig.EmbeddedModeConfig.class,
        MongoTestConfig.ContainerModeConfig.class
})
public class MongoTestConfig {

    @TestConfiguration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "test.mongo.mode", havingValue = "embedded", matchIfMissing = true)
    static class EmbeddedModeConfig {
    }

    @TestConfiguration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "test.mongo.mode", havingValue = "container")
    @ImportAutoConfiguration(exclude = EmbeddedMongoAutoConfiguration.class)
    static class ContainerModeConfig {

        @Bean
        @ServiceConnection
        MongoDBContainer mongoDBContainer(
                @Value("${test.mongo.image:mongo:7.0.14}") String image
        ) {
            return new MongoDBContainer(
                    DockerImageName.parse(image)
            );
        }
    }
}
