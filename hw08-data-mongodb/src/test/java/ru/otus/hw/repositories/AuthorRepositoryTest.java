package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import ru.otus.hw.CommonContext;
import ru.otus.hw.models.Author;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class AuthorRepositoryTest extends CommonContext {

    @Autowired
    private AuthorRepository repository;


    @BeforeEach
    void setUp() {
        repository.deleteAll();
        repository.saveAll(List.of(
                new Author("a1", "Author_1"),
                new Author("a2", "Author_2"),
                new Author("a3", "Author_3")
        ));
    }

    @Test
    @DisplayName("should load all authors")
    void findAll() {
        var list = repository.findAll();
        assertThat(list).extracting(Author::getId)
                .containsExactlyInAnyOrder("a1", "a2", "a3");
        assertThat(list).extracting(Author::getFullName)
                .containsExactlyInAnyOrder("Author_1", "Author_2", "Author_3");
    }


    @Nested
    @DisplayName("findById")
    class FindById {
        @Test
        @DisplayName("should return author when exists")
        void returnsAuthor() {
            assertThat(repository.findById("a2")).get()
                    .extracting(Author::getFullName).isEqualTo("Author_2");
        }

        @Test
        @DisplayName("should return empty when not exists")
        void returnsEmpty() {
            assertThat(repository.findById("missing")).isEmpty();
        }
    }
}
