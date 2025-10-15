package ru.otus.hw.config;


import feign.RequestInterceptor;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "ru.otus.hw.integration")
public class FeignConfig {

    @Bean
    public RequestInterceptor userAgent() {
        return template -> template.header("User-Agent", "library-service/1.0");
    }
}
