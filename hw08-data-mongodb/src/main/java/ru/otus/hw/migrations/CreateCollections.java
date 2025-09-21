package ru.otus.hw.migrations;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import ru.otus.hw.utils.MigrationUtils;

import java.util.List;


@RequiredArgsConstructor
@ChangeUnit(
        id = "001-create-collections-and-validators",
        order = "001",
        author = "Viktor Kogai"
)
public class CreateCollections {

    private final MongoTemplate mongo;

    @Execution
    public void up() {
        // authors
        var authorsProps = new Document()
                .append("fullName", new Document("bsonType", "string").append("minLength", 1));
        var authorsSchema = MigrationUtils.objectSchema(authorsProps, "fullName");
        MigrationUtils.applyJsonSchemaValidator(mongo, "authors", authorsSchema);

        // genres
        var genresProps = new Document()
                .append("name", new Document("bsonType", "string").append("minLength", 1));
        var genresSchema = MigrationUtils.objectSchema(genresProps, "name");
        MigrationUtils.applyJsonSchemaValidator(mongo, "genres", genresSchema);

        // books
        var dbRefSchema = new Document("bsonType", "object")
                .append("required", List.of("$id"))
                .append("properties", new Document()
                        .append("$id", new Document("bsonType", List.of("string", "objectId")))
                        .append("$ref", new Document("bsonType", "string"))
                        .append("$db", new Document("bsonType", "string")));

        var booksProps = new Document()
                .append("title", new Document("bsonType", "string").append("minLength", 1))
                .append("author", dbRefSchema)
                .append("genres", new Document("bsonType", "array").append("items", dbRefSchema));
        var booksSchema = MigrationUtils.objectSchema(booksProps, "title", "author");
        MigrationUtils.applyJsonSchemaValidator(mongo, "books", booksSchema);

        // comments
        var commentsProps = new Document()
                .append("text", new Document("bsonType", "string").append("minLength", 1))
                .append("bookId", new Document("bsonType", "string").append("minLength", 1))
                .append("createdAt", new Document("bsonType", "date"));
        var commentsSchema = MigrationUtils.objectSchema(commentsProps, "text", "bookId", "createdAt");
        MigrationUtils.applyJsonSchemaValidator(mongo, "comments", commentsSchema);
    }


    @RollbackExecution
    public void rollback() { }

}
