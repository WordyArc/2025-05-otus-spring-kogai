package ru.otus.hw.migration.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.migration.service.IdMappingService;
import ru.otus.hw.mongo.models.AuthorDocument;

@Service
@RequiredArgsConstructor
public class AuthorMapper {
    private final IdMappingService ids;

    public AuthorDocument toDocument(ru.otus.hw.jpa.models.Author a) {
        var oid = ids.resolve("author", String.valueOf(a.getId()));
        return new AuthorDocument(oid, a.getFullName());
    }
}
