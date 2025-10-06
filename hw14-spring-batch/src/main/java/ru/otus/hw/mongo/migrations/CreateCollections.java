package ru.otus.hw.mongo.migrations;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import ru.otus.hw.mongo.utils.MigrationUtils;

import java.util.List;


@RequiredArgsConstructor
@ChangeUnit(
        id = "001-create-collections-and-validators",
        order = "001",
        author = "Viktor Kogai"
)
public class CreateCollections {

    private static final String C_AUTHORS  = "authors";

    private static final String C_GENRES   = "genres";

    private static final String C_BOOKS    = "books";

    private static final String C_COMMENTS = "comments";

    private final MongoTemplate mongo;

    @Execution
    public void up() {
        createAuthorsCollection();
        createGenresCollection();
        createBooksCollection();
        createCommentsCollection();
    }

    private void createAuthorsCollection() {
        var props = new Document()
                .append("fullName", stringSchema(1));
        var schema = MigrationUtils.objectSchema(props, "fullName");
        apply(C_AUTHORS, schema);
    }

    private void createGenresCollection() {
        var props = new Document()
                .append("name", stringSchema(1));
        var schema = MigrationUtils.objectSchema(props, "name");
        apply(C_GENRES, schema);
    }

    private void createBooksCollection() {
        var dbRef = dbRefSchema();
        var props = new Document()
                .append("title", stringSchema(1))
                .append("author", dbRef)
                .append("genres", arrayOf(dbRef));
        var schema = MigrationUtils.objectSchema(props, "title", "author");
        apply(C_BOOKS, schema);
    }

    private void createCommentsCollection() {
        var props = new Document()
                .append("text", stringSchema(1))
                .append("bookId", objectIdOrString())
                .append("createdAt", dateSchema());
        var schema = MigrationUtils.objectSchema(props, "text", "bookId", "createdAt");
        apply(C_COMMENTS, schema);
    }

    private void apply(String collection, Document schema) {
        MigrationUtils.applyJsonSchemaValidator(mongo, collection, schema);
    }

    private static Document stringSchema(int minLength) {
        return new Document("bsonType", "string").append("minLength", minLength);
    }

    private static Document objectIdOrString() {
        return new Document("bsonType", List.of("objectId", "string"));
    }

    private static Document dateSchema() {
        return new Document("bsonType", "date");
    }

    private static Document arrayOf(Document itemSchema) {
        return new Document("bsonType", "array").append("items", itemSchema);
    }

    private static Document dbRefSchema() {
        return new Document("bsonType", "object")
                .append("required", List.of("$id"))
                .append("properties", new Document()
                        .append("$id", new Document("bsonType", List.of("string", "objectId")))
                        .append("$ref", new Document("bsonType", "string"))
                        .append("$db", new Document("bsonType", "string")));
    }

    @RollbackExecution
    public void rollback() {
    }
}
