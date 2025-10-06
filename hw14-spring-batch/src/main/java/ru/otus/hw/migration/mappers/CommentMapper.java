package ru.otus.hw.migration.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.migration.service.IdMappingService;
import ru.otus.hw.mongo.models.CommentDocument;

@Service
@RequiredArgsConstructor
public class CommentMapper {
    private final IdMappingService ids;

    public CommentDocument toDocument(ru.otus.hw.jpa.models.Comment c) {
        var cid = ids.resolve("comment", String.valueOf(c.getId()));
        var bookOid = ids.resolve("book", String.valueOf(c.getBook().getId()));
        return new CommentDocument(cid, c.getText(), bookOid, c.getCreatedAt(), null);
    }
}
