package ru.otus.hw.batch.rdbms2mongo.idmap;

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
import ru.otus.hw.config.BatchProperties;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class IdMappingService {

    private final IdMappingRepository repository;

    private final MongoOperations operations;

    private final BatchProperties batchProperties;

    private final Map<String, Cache<@NonNull String, ObjectId>> caches = new ConcurrentHashMap<>();

    public ObjectId resolve(String srcType, String srcId) {
        long cap = sizeFor(srcType);
        if (cap <= 0) {
            return upsertAndReturnTargetId(srcType, srcId);
        }

        return cacheFor(srcType).get(srcId, key -> upsertAndReturnTargetId(srcType, key));
    }

    private ObjectId upsertAndReturnTargetId(String srcType, String srcId) {
        var query = Query.query(Criteria.where("sourceType").is(srcType).and("sourceId").is(srcId));
        var update = new Update()
                .setOnInsert("sourceType", srcType)
                .setOnInsert("sourceId", srcId)
                .setOnInsert("targetId", new ObjectId());
        var opts = FindAndModifyOptions.options().returnNew(true).upsert(true);

        var saved = Objects.requireNonNull(
                operations.findAndModify(query, update, opts, IdMappingDocument.class, "id_mappings"),
                "id_mappings upsert must return a document"
        );

        return saved.getTargetId();
    }

    private Cache<@NonNull String, ObjectId> cacheFor(String type) {
        return caches.computeIfAbsent(type, t ->
                Caffeine.newBuilder()
                        .maximumSize(sizeFor(t))
                        .build()
        );
    }

    private long sizeFor(String type) {
        return batchProperties.getIdMappingCache().getPerType()
                .getOrDefault(type, batchProperties.getIdMappingCache().getMaximumSize());
    }
}
