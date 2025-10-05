package ru.otus.hw.mongo.migrations;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import ru.otus.hw.mongo.models.AuthorDocument;
import ru.otus.hw.mongo.models.BookDocument;
import ru.otus.hw.mongo.models.GenreDocument;
import ru.otus.hw.mongo.repositories.MongoAuthorRepository;
import ru.otus.hw.mongo.repositories.MongoBookRepository;
import ru.otus.hw.mongo.repositories.MongoGenreRepository;

import java.util.List;

@Profile("dev")
@RequiredArgsConstructor
@ChangeUnit(id = "dev-init-data", order = "900", author = "Viktor Kogai")
public class InitDataChangeUnit {

    private final MongoAuthorRepository authorRepository;

    private final MongoGenreRepository genreRepository;

    private final MongoBookRepository bookRepository;

    @Execution
    public void seed() {
        var a1 = authorRepository.save(new AuthorDocument("a1", "Author_1"));
        var a2 = authorRepository.save(new AuthorDocument("a2", "Author_2"));
        var a3 = authorRepository.save(new AuthorDocument("a3", "Author_3"));

        var g1 = genreRepository.save(new GenreDocument("g1", "Genre_1"));
        var g2 = genreRepository.save(new GenreDocument("g2", "Genre_2"));
        var g3 = genreRepository.save(new GenreDocument("g3", "Genre_3"));
        var g4 = genreRepository.save(new GenreDocument("g4", "Genre_4"));
        var g5 = genreRepository.save(new GenreDocument("g5", "Genre_5"));
        var g6 = genreRepository.save(new GenreDocument("g6", "Genre_6"));

        bookRepository.save(new BookDocument("b1", "BookTitle_1", a1, List.of(g1, g2)));
        bookRepository.save(new BookDocument("b2", "BookTitle_2", a2, List.of(g3, g4)));
        bookRepository.save(new BookDocument("b3", "BookTitle_3", a3, List.of(g5, g6)));
    }

    @RollbackExecution
    public void rollback() {
        bookRepository.deleteAll();
        genreRepository.deleteAll();
        authorRepository.deleteAll();
    }

}
