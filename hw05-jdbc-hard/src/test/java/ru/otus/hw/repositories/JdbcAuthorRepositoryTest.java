package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Author;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(JdbcAuthorRepository.class)
class JdbcAuthorRepositoryTest {

    @Autowired
    private JdbcAuthorRepository repository;

    @Test
    @DisplayName("should load all authors in deterministic order")
    void shouldFindAll() {
        var authors = repository.findAll();
        assertThat(authors).containsExactly(
                new Author(1, "Author_1"),
                new Author(2, "Author_2"),
                new Author(3, "Author_3")
        );
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should find author by id")
        void shouldFindExisting() {
            var author = repository.findById(2L);
            assertThat(author).isPresent();
            assertThat(author.get()).isEqualTo(new Author(2, "Author_2"));
        }

        @Test
        @DisplayName("should return empty when author not found")
        void shouldReturnEmptyWhenMissing() {
            var author = repository.findById(999L);
            assertThat(author).isEmpty();
        }
    }
}