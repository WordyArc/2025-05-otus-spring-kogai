package ru.otus.hw.batch.r2m;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.Assert;
import ru.otus.hw.persistence.mongo.model.AuthorDocument;
import ru.otus.hw.persistence.mongo.model.BookDocument;
import ru.otus.hw.persistence.mongo.model.CommentDocument;
import ru.otus.hw.persistence.mongo.model.GenreDocument;

@Configuration
@RequiredArgsConstructor
public class R2mWritersConfig {

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
        return chunk -> {
            var items = chunk.getItems();
            if (items.isEmpty()) return;

            BulkOperations ops = operations.bulkOps(BulkOperations.BulkMode.UNORDERED, collection);

            for (T it : items) {
                var doc = new Document();
                operations.getConverter().write(it, doc);

                Object id = doc.get("_id");
                Assert.notNull(id, "Mongo document id must not be null");

                var query = Query.query(Criteria.where("_id").is(id));
                ops.replaceOne(query, doc, new FindAndReplaceOptions().upsert());
            }

            ops.execute();
        };
    }
}
