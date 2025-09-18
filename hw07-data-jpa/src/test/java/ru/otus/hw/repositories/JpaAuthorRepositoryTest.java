package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Author;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuthorRepository.class)
class JpaAuthorRepositoryTest {

    @Autowired
    private JpaAuthorRepository repository;

    @Test
    @DisplayName("should load all authors in deterministic order")
    void findAll() {
        var list = repository.findAll();
        assertThat(list).extracting(Author::getId).containsExactly(1L, 2L, 3L);
        assertThat(list).extracting(Author::getFullName)
                .containsExactly("Author_1", "Author_2", "Author_3");
    }


    @Nested
    @DisplayName("findById")
    class FindById {
        @Test
        @DisplayName("should return author when exists")
        void returnsAuthor() {
            assertThat(repository.findById(2L)).get()
                    .extracting(Author::getFullName).isEqualTo("Author_2");
        }

        @Test
        @DisplayName("should return empty when not exists")
        void returnsEmpty() {
            assertThat(repository.findById(999L)).isEmpty();
        }
    }
}
