package ru.otus.hw.mappers;

import lombok.experimental.UtilityClass;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.models.Comment;

import java.time.format.DateTimeFormatter;

@UtilityClass
public class CommentMapper {

    public CommentDto toDto(Comment c) {
        var created = c.getCreatedAt() == null
                ? "-"
                : DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(c.getCreatedAt());
        return new CommentDto(c.getId(), c.getText(), created);
    }

}