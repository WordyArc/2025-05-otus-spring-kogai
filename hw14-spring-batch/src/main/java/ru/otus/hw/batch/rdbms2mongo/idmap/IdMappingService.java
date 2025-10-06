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
        var cached = getCache().getIfPresent(key);
        if (cached != null) return cached;

        var query = Query.query(Criteria.where("sourceType").is(srcType).and("sourceId").is(srcId));
        var update = new Update()
                .setOnInsert("sourceType", srcType)
                .setOnInsert("sourceId", srcId)
                .setOnInsert("targetId", new ObjectId());
        var opts = FindAndModifyOptions.options().returnNew(true).upsert(true);

        var saved = Objects.requireNonNull(
                operations.findAndModify(query, update, opts, IdMappingDocument.class, "id_mappings"));
        var resolved = saved.getTargetId();
        cache.put(key, resolved);
        return resolved;
    }
}
