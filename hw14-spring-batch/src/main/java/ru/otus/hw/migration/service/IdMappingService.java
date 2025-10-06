package ru.otus.hw.migration.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NonNull;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import ru.otus.hw.migration.models.IdMappingDocument;
import ru.otus.hw.migration.repositories.IdMappingRepository;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class IdMappingService {

    private final IdMappingRepository repository;

    private final MongoOperations operations;

    private final Cache<@NonNull String, ObjectId> cache = Caffeine.newBuilder()
            .maximumSize(500_000)
            .build();

    public ObjectId resolve(String srcType, String srcId) {
        var key = srcType + ":" + srcId;
        ObjectId cached = cache.getIfPresent(key);
        if (cached != null) return cached;

        var query = Query.query(Criteria.where("sourceType").is(srcType).and("sourceId").is(srcId));
        var newId = new ObjectId();
        var update = new Update()
                .setOnInsert("sourceType", srcType)
                .setOnInsert("sourceId", srcId)
                .setOnInsert("targetId", newId);

        var opts = FindAndModifyOptions.options().upsert(true).returnNew(true);
        var saved = Objects.requireNonNull(
                operations.findAndModify(query, update, opts, IdMappingDocument.class, "id_mappings")
        );

        return saved.getTargetId();
    }
}
