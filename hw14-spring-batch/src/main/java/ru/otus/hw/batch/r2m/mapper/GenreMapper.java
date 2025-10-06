package ru.otus.hw.batch.r2m.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.persistence.rdbms.model.Genre;
import ru.otus.hw.batch.r2m.idmap.IdMappingService;
import ru.otus.hw.persistence.mongo.model.GenreDocument;

@Service
@RequiredArgsConstructor
public class GenreMapper {
    private final IdMappingService ids;

    public GenreDocument toDocument(Genre g) {
        var oid = ids.resolve("genre", String.valueOf(g.getId()));
        return new GenreDocument(oid, g.getName());
    }
}
