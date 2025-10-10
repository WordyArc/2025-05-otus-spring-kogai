package ru.otus.hw.projections;

import org.springframework.data.rest.core.config.Projection;
import ru.otus.hw.models.Book;

import java.util.List;

@Projection(name = "bookDetail", types = {Book.class})
public interface BookDetailProjection {

    Long getId();

    String getTitle();

    AuthorProjection getAuthor();

    List<GenreProjection> getGenres();

    interface AuthorProjection {
        Long getId();

        String getFullName();
    }

    interface GenreProjection {
        Long getId();

        String getName();
    }
}
