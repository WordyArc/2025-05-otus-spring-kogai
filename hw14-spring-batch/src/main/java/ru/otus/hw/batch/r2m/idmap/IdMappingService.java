package ru.otus.hw.batch.r2m.idmap;

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

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class IdMappingService {

    private final IdMappingRepository repository;

    private final MongoOperations operations;

    private final BatchProperties batchProperties;

    private Cache<@NonNull String, ObjectId> cache;

    private Cache<@NonNull String, ObjectId> getCache() {
        if (cache == null) {
            cache = Caffeine.newBuilder()
                    .maximumSize(batchProperties.getIdMappingCache().getMaximumSize())
                    .build();
        }
        return cache;
    }

    public ObjectId resolve(String srcType, String srcId) {
        var key = srcType + ":" + srcId;
        ObjectId cached = getCache().getIfPresent(key);
        if (cached != null) return cached;

        var existing = repository.findBySourceTypeAndSourceId(srcType, srcId).map(IdMappingDocument::getTargetId);
        if (existing.isPresent()) {
            cache.put(key, existing.get());
            return existing.get();
        }

        var newId = new ObjectId();
        var query = new Query(Criteria.where("sourceType").is(srcType).and("sourceId").is(srcId));
        var update = new Update()
                .setOnInsert("sourceType", srcType)
                .setOnInsert("sourceId", srcId)
                .setOnInsert("targetId", newId);
        var opts = FindAndModifyOptions.options().returnNew(true).upsert(true);

        var saved = operations.findAndModify(query, update, opts, IdMappingDocument.class, "id_mappings");
        ObjectId resolved = Objects.requireNonNull(saved).getTargetId();
        cache.put(key, resolved);
        return resolved;
    }
}
