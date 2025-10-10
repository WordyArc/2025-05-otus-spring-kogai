package ru.otus.hw.projections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;
import ru.otus.hw.models.Comment;

import java.time.LocalDateTime;

/**
 * Projection for Comment entity with book details.
 * Used by Spring Data REST to provide HATEOAS-compliant responses.
 */
@Projection(name = "commentDetail", types = {Comment.class})
public interface CommentDetailProjection {

    Long getId();

    String getText();

    LocalDateTime getCreatedAt();

    BookProjection getBook();

    /**
     * Virtual property - book title for convenience.
     */
    @Value("#{target.book.title}")
    String getBookTitle();

    /**
     * Inline projection for Book to avoid cyclic references.
     */
    interface BookProjection {
        Long getId();
        String getTitle();
    }
}
