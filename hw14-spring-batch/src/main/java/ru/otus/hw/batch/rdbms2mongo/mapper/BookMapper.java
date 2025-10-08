package ru.otus.hw.batch.rdbms2mongo.mapper;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.persistence.rdbms.model.Book;
import ru.otus.hw.batch.rdbms2mongo.idmap.IdMappingService;
import ru.otus.hw.persistence.mongo.model.AuthorDocument;
import ru.otus.hw.persistence.mongo.model.BookDocument;
import ru.otus.hw.persistence.mongo.model.GenreDocument;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class BookMapper {
    private final IdMappingService ids;

    public BookDocument toDocument(Book b) {
        var bid = ids.resolve("book", String.valueOf(b.getId()));
        var aid = ids.resolve("author", String.valueOf(b.getAuthor().getId()));
        var author = new AuthorDocument(aid, b.getAuthor().getFullName());

        var genres = b.getGenres().stream()
                .map(g -> new GenreDocument(
                        ids.resolve("genre", String.valueOf(g.getId())),
                        g.getName()))
                .toList();

        return new BookDocument(bid, b.getTitle(), author, new ArrayList<>(genres));
    }
}

