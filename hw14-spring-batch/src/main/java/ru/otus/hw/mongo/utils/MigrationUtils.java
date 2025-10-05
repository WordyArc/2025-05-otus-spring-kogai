package ru.otus.hw.mongo.utils;

import com.mongodb.client.MongoDatabase;
import lombok.experimental.UtilityClass;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.HashSet;
import java.util.List;

@UtilityClass
public class MigrationUtils {

    public boolean collectionExists(MongoTemplate template, String name) {
        MongoDatabase db = template.getDb();
        var names = new HashSet<String>();
        db.listCollectionNames().into(names);
        return names.contains(name);
    }

    public void createCollectionIfMissing(MongoTemplate template, String name) {
        if (!collectionExists(template, name)) {
            template.createCollection(name);
        }
    }

    public void applyJsonSchemaValidator(MongoTemplate template, String collection, Document jsonSchema) {
        createCollectionIfMissing(template, collection);
        var cmd = new Document("collMod", collection)
                .append("validator", new Document("$jsonSchema", jsonSchema))
                .append("validationLevel", "strict")
                .append("validationAction", "error");
        template.getDb().runCommand(cmd);
    }

    public Document objectSchema(Document properties, String... required) {
        return new Document("bsonType", "object")
                .append("properties", properties)
                .append("required", required == null
                        ? List.of()
                        : List.of(required)
                );
    }

}
