package ru.otus.hw.persistence.mongo.repository;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import ru.otus.hw.persistence.mongo.model.AuthorDocument;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class MongoAuthorRepositoryTest {

    @Autowired
    private MongoAuthorRepository repository;


    @BeforeEach
    void setUp() {
        repository.deleteAll();
        repository.saveAll(List.of(
                new AuthorDocument(null, "Author_1"),
                new AuthorDocument(null, "Author_2"),
                new AuthorDocument(null, "Author_3")
        ));
    }

    @Test
    @DisplayName("should load all authors")
    void findAll() {
        var list = repository.findAll();
        assertThat(list).hasSize(3);
        assertThat(list).extracting(AuthorDocument::getFullName)
                .containsExactlyInAnyOrder("Author_1", "Author_2", "Author_3");
    }


    @Nested
    @DisplayName("findById")
    class FindById {
        
        private ObjectId savedId;
        
        @BeforeEach
        void setupId() {
            savedId = repository.findAll().stream()
                    .filter(a -> "Author_2".equals(a.getFullName()))
                    .findFirst()
                    .orElseThrow()
                    .getId();
        }
        
        @Test
        @DisplayName("should return author when exists")
        void returnsAuthor() {
            assertThat(repository.findById(savedId)).get()
                    .extracting(AuthorDocument::getFullName).isEqualTo("Author_2");
        }

        @Test
        @DisplayName("should return empty when not exists")
        void returnsEmpty() {
            assertThat(repository.findById(new ObjectId())).isEmpty();
        }
    }
}
