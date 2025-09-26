package ru.otus.hw.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.otus.hw.TestData;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

@TestConfiguration
public class TestDataConfig {

    @Bean
    public TestData testData(AuthorRepository authors,
                             GenreRepository genres,
                             BookRepository books,
                             CommentRepository comments) {
        return new TestData(authors, genres, books, comments);
    }
}
