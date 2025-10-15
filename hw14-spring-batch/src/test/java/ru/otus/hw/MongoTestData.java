package ru.otus.hw;

import ru.otus.hw.persistence.mongo.model.AuthorDocument;
import ru.otus.hw.persistence.mongo.model.BookDocument;
import ru.otus.hw.persistence.mongo.model.GenreDocument;
import ru.otus.hw.persistence.mongo.repository.MongoAuthorRepository;
import ru.otus.hw.persistence.mongo.repository.MongoBookRepository;
import ru.otus.hw.persistence.mongo.repository.MongoCommentRepository;
import ru.otus.hw.persistence.mongo.repository.MongoGenreRepository;

import java.util.List;

public record MongoTestData(MongoAuthorRepository authors,
                            MongoGenreRepository genres,
                            MongoBookRepository books,
                            MongoCommentRepository comments) {

    public void cleanAll() {
        comments.deleteAll();
        books.deleteAll();
        genres.deleteAll();
        authors.deleteAll();
    }

    public void seedBasic() {
        var a1 = authors.save(new AuthorDocument(null, "Author_1"));
        var a2 = authors.save(new AuthorDocument(null, "Author_2"));
        var a3 = authors.save(new AuthorDocument(null, "Author_3"));

        var g1 = genres.save(new GenreDocument(null, "Genre_1"));
        var g2 = genres.save(new GenreDocument(null, "Genre_2"));
        var g3 = genres.save(new GenreDocument(null, "Genre_3"));
        var g4 = genres.save(new GenreDocument(null, "Genre_4"));
        var g5 = genres.save(new GenreDocument(null, "Genre_5"));
        var g6 = genres.save(new GenreDocument(null, "Genre_6"));

        books.save(new BookDocument(null, "BookTitle_1", a1, List.of(g1, g2)));
        books.save(new BookDocument(null, "BookTitle_2", a2, List.of(g3, g4)));
        books.save(new BookDocument(null, "BookTitle_3", a3, List.of(g5, g6)));
    }

    public void resetAndSeed() {
        cleanAll();
        seedBasic();
    }
}
