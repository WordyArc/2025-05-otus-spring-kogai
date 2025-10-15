package ru.otus.hw.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.otus.hw.MongoTestData;
import ru.otus.hw.persistence.mongo.repository.MongoAuthorRepository;
import ru.otus.hw.persistence.mongo.repository.MongoBookRepository;
import ru.otus.hw.persistence.mongo.repository.MongoCommentRepository;
import ru.otus.hw.persistence.mongo.repository.MongoGenreRepository;

@TestConfiguration
public class TestDataConfig {

    @Bean
    public MongoTestData testData(MongoAuthorRepository authors,
                                  MongoGenreRepository genres,
                                  MongoBookRepository books,
                                  MongoCommentRepository comments) {
        return new MongoTestData(authors, genres, books, comments);
    }
}
