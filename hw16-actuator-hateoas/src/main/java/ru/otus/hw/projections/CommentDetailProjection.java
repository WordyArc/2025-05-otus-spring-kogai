package ru.otus.hw.projections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;
import ru.otus.hw.models.Comment;

import java.time.LocalDateTime;

@Projection(name = "commentDetail", types = {Comment.class})
public interface CommentDetailProjection {

    Long getId();

    String getText();

    LocalDateTime getCreatedAt();

    BookProjection getBook();

    @Value("#{target.book.title}")
    String getBookTitle();

    interface BookProjection {
        Long getId();

        String getTitle();
    }
}
