package ru.otus.hw.db.mongo.migration;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.Index;

@ChangeUnit(id = "003-create-id-map", order = "003", author = "Viktor Kogai")
@RequiredArgsConstructor
public class CreateIdMap {

    private final MongoOperations operations;

    @Execution
    public void up() {
        if (!operations.collectionExists("id_mappings")) {
            operations.createCollection("id_mappings");
        }
        operations.indexOps("id_mappings").createIndex(
                new Index().on("sourceType", Sort.Direction.ASC)
                        .on("sourceId", Sort.Direction.ASC)
                        .unique()
                        .named("sourceType_sourceId_unique")
        );
    }

    @RollbackExecution
    public void rollback() {
        operations.dropCollection("id_mappings");
    }
}
