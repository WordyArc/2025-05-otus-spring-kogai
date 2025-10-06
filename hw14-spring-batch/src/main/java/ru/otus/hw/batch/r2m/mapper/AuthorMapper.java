package ru.otus.hw.batch.r2m.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.persistence.rdbms.model.Author;
import ru.otus.hw.batch.r2m.idmap.IdMappingService;
import ru.otus.hw.persistence.mongo.model.AuthorDocument;

@Service
@RequiredArgsConstructor
public class AuthorMapper {
    private final IdMappingService ids;

    public AuthorDocument toDocument(Author a) {
        var oid = ids.resolve("author", String.valueOf(a.getId()));
        return new AuthorDocument(oid, a.getFullName());
    }
}
