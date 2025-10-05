package ru.otus.hw.jpa.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.jpa.models.Book;
import ru.otus.hw.jpa.repositories.BookRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookRepositoryTest {

    @Autowired
    private BookRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("should find book by id with author and genres")
    void shouldFindById() {
        var book = repository.findById(1L).orElseThrow();
        assertThat(book.getTitle()).isEqualTo("BookTitle_1");
        assertThat(book.getAuthor().getFullName()).isEqualTo("Author_1");
        assertThat(book.getGenres()).hasSize(2);
    }

    @Test
    @DisplayName("should return all books with author and genres")
    void shouldFindAll() {
        var list = repository.findAll();
        assertThat(list).hasSize(3);
        list.forEach(b -> {
            assertThat(b.getAuthor().getFullName()).isNotBlank();
            assertThat(b.getGenres()).isNotEmpty();
        });
    }

    @Test
    @DisplayName("should delete existing book")
    void deleteExisting() {
        repository.deleteById(1L);
        entityManager.flush(); entityManager.clear();
        assertThat(entityManager.find(Book.class, 1L)).isNull();
    }

}
