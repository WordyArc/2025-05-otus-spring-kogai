package ru.otus.hw.migration.batch;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.Assert;
import ru.otus.hw.mongo.models.AuthorDocument;
import ru.otus.hw.mongo.models.BookDocument;
import ru.otus.hw.mongo.models.CommentDocument;
import ru.otus.hw.mongo.models.GenreDocument;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class BatchWriterConfig {

    private final MongoOperations operations;

    @Bean
    @StepScope
    public ItemWriter<AuthorDocument> authorWriter() {
        return bulkUpsert("authors");
    }

    @Bean
    @StepScope
    public ItemWriter<GenreDocument> genreWriter() {
        return bulkUpsert("genres");
    }

    @Bean
    @StepScope
    public ItemWriter<BookDocument> bookWriter() {
        return bulkUpsert("books");
    }

    @Bean
    @StepScope
    public ItemWriter<CommentDocument> commentWriter() {
        return bulkUpsert("comments");
    }

    private <T> ItemWriter<T> bulkUpsert(String collection) {
        return (Chunk<? extends T> chunk) -> {
            List<? extends T> items = chunk.getItems();
            if (items.isEmpty()) {
                return;
            }
            BulkOperations ops = operations.bulkOps(BulkOperations.BulkMode.UNORDERED, collection);

            for (T it : items) {
                var doc = new Document();
                operations.getConverter().write(it, doc);

                Object id = doc.get("_id");
                Assert.notNull(id, "Mongo document id must not be null");

                doc.remove("_id");

                //var update = new Update();
                //doc.forEach(update::set);
                var update = Update.fromDocument(new Document("$set", doc));
                var query = Query.query(Criteria.where("_id").is(id));
                ops.upsert(query, update);
            }
            ops.execute();
        };
    }
}
