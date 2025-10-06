package ru.otus.hw.batch.rdbms2mongo.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.persistence.rdbms.model.Comment;
import ru.otus.hw.batch.rdbms2mongo.idmap.IdMappingService;
import ru.otus.hw.persistence.mongo.model.CommentDocument;

@Service
@RequiredArgsConstructor
public class CommentMapper {
    private final IdMappingService ids;

    public CommentDocument toDocument(Comment c) {
        var cid = ids.resolve("comment", String.valueOf(c.getId()));
        var bookOid = ids.resolve("book", String.valueOf(c.getBook().getId()));
        return new CommentDocument(cid, c.getText(), bookOid, c.getCreatedAt(), null);
    }
}
