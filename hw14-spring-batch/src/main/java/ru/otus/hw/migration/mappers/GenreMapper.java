package ru.otus.hw.migration.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.migration.service.IdMappingService;
import ru.otus.hw.mongo.models.GenreDocument;

@Service
@RequiredArgsConstructor
public class GenreMapper {
    private final IdMappingService ids;

    public GenreDocument toDocument(ru.otus.hw.jpa.models.Genre g) {
        var oid = ids.resolve("genre", String.valueOf(g.getId()));
        return new GenreDocument(oid, g.getName());
    }
}
