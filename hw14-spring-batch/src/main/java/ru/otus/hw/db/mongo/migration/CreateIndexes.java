package ru.otus.hw.db.mongo.migration;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

@RequiredArgsConstructor
@ChangeUnit(
        id = "002-create-indexes",
        order = "002",
        author = "Viktor Kogai")
public class CreateIndexes {

    private final MongoTemplate mongo;

    @Execution
    public void up() {
        ensureUnique("genres", "name");
        ensureUnique("authors", "fullName");

        index("comments", "bookId");

        index("comments", "createdAt");
    }

    private void ensureUnique(String collection, String field) {
        IndexOperations operations = mongo.indexOps(collection);
        operations.createIndex(new Index().on(field, Sort.Direction.ASC).unique());
    }

    private void index(String collection, String field) {
        IndexOperations operations = mongo.indexOps(collection);
        operations.createIndex(new Index().on(field, Sort.Direction.ASC));
    }

    @RollbackExecution
    public void rollback() {
    }

}
