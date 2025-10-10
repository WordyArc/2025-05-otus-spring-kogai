package ru.otus.hw.projections;

import org.springframework.data.rest.core.config.Projection;
import ru.otus.hw.models.Book;

import java.util.List;

/**
 * Projection for Book entity with full details.
 * Used by Spring Data REST to provide HATEOAS-compliant responses.
 */
@Projection(name = "bookDetail", types = {Book.class})
public interface BookDetailProjection {

    Long getId();

    String getTitle();

    AuthorProjection getAuthor();

    List<GenreProjection> getGenres();

    /**
     * Inline projection for Author to avoid cyclic references.
     */
    interface AuthorProjection {
        Long getId();
        String getFullName();
    }

    /**
     * Inline projection for Genre to avoid cyclic references.
     */
    interface GenreProjection {
        Long getId();
        String getName();
    }
}
