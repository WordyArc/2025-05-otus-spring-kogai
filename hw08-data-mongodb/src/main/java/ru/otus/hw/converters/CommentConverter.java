package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.models.Comment;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
public class CommentConverter {

    public String commentToString(Comment c) {
        var created = Optional.ofNullable(c.getCreatedAt())
                .map(DateTimeFormatter.ISO_LOCAL_DATE_TIME::format)
                .orElse("-");

        var bookInfo = (c.getBook() == null)
                ? "null"
                : "id=%s, title=%s".formatted(c.getBook().getId(), c.getBook().getTitle());

        return "Id: %s, book: [%s], text: %s, createdAt: %s"
                .formatted(c.getId(), bookInfo, c.getText(), created);
    }
}
