package ru.otus.hw.persistence.rdbms.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.persistence.rdbms.model.Author;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AuthorRepositoryTest {

    @Autowired
    private AuthorRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("should load all authors")
    void findAll() {
        var author1 = entityManager.persistAndFlush(new Author(null, "Author 1"));
        var author2 = entityManager.persistAndFlush(new Author(null, "Author 2"));
        var author3 = entityManager.persistAndFlush(new Author(null, "Author 3"));

        var list = repository.findAll();
        assertThat(list)
                .hasSize(3)
                .extracting(Author::getId)
                .containsExactlyInAnyOrder(author1.getId(), author2.getId(), author3.getId());
        assertThat(list)
                .extracting(Author::getFullName)
                .containsExactlyInAnyOrder("Author 1", "Author 2", "Author 3");
    }

    @Nested
    @DisplayName("findById")
    class FindById {
        @Test
        @DisplayName("should return author when exists")
        void returnsAuthor() {
            var author = entityManager.persistAndFlush(new Author(null, "Test Author"));
            
            assertThat(repository.findById(author.getId())).get()
                    .extracting(Author::getFullName).isEqualTo("Test Author");
        }

        @Test
        @DisplayName("should return empty when not exists")
        void returnsEmpty() {
            assertThat(repository.findById(999L)).isEmpty();
        }
    }
}
