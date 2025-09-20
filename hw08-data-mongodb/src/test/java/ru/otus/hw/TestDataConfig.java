package ru.otus.hw;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;

@TestConfiguration
public class TestDataConfig {

    @Bean
    InitializingBean seed(
            AuthorRepository authors,
            GenreRepository genres,
            BookRepository books
    ) {
        return () -> {
            books.deleteAll();
            genres.deleteAll();
            authors.deleteAll();

            var a1 = authors.save(new Author("a1", "Author_1"));
            var a2 = authors.save(new Author("a2", "Author_2"));
            var a3 = authors.save(new Author("a3", "Author_3"));

            var g1 = genres.save(new Genre("g1", "Genre_1"));
            var g2 = genres.save(new Genre("g2", "Genre_2"));
            var g3 = genres.save(new Genre("g3", "Genre_3"));
            var g4 = genres.save(new Genre("g4", "Genre_4"));
            var g5 = genres.save(new Genre("g5", "Genre_5"));
            var g6 = genres.save(new Genre("g6", "Genre_6"));

            books.save(new Book("b1", "BookTitle_1", a1, List.of(g1, g2)));
            books.save(new Book("b2", "BookTitle_2", a2, List.of(g3, g4)));
            books.save(new Book("b3", "BookTitle_3", a3, List.of(g5, g6)));
        };
    }
}