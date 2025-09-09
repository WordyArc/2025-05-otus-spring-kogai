package ru.otus.hw.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan(basePackages = "ru.otus.hw")
@PropertySource("classpath:application.properties")
public class AppConfig {

    @Bean
    public AppProperties appProperties(@Value("${test.rightAnswersCountToPass}") int rightAnswersCountToPass,
                                       @Value("${test.fileName}") String testFileName) {
        AppProperties properties = new AppProperties();
        properties.setTestFileName(testFileName);
        properties.setRightAnswersCountToPass(rightAnswersCountToPass);
        return properties;
    }
}
