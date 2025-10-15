package ru.otus.hw.persistence.rdbms.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.persistence.rdbms.model.Author;
import ru.otus.hw.persistence.rdbms.model.Book;
import ru.otus.hw.persistence.rdbms.model.Genre;

import java.util.ArrayList;

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
        var persisted = persistBook("Test Book", "Author", "Novel", "Drama");
        entityManager.flush();
        entityManager.clear();

        var book = repository.findById(persisted.getId()).orElseThrow();
        assertThat(book.getTitle()).isEqualTo("Test Book");
        assertThat(book.getAuthor().getFullName()).isEqualTo("Author");
        assertThat(book.getGenres()).extracting(Genre::getName)
                .containsExactlyInAnyOrder("Novel", "Drama");
    }

    @Test
    @DisplayName("should return all books with author and genres")
    void shouldFindAll() {
        var book1 = persistBook("Book One", "Author One", "Genre A");
        var book2 = persistBook("Book Two", "Author Two", "Genre B", "Genre C");
        var book3 = persistBook("Book Three", "Author Three", "Genre D");
        entityManager.flush();
        entityManager.clear();

        var list = repository.findAll();
        assertThat(list).hasSize(3);
        assertThat(list).extracting(Book::getId)
                .containsExactlyInAnyOrder(book1.getId(), book2.getId(), book3.getId());
        list.forEach(b -> {
            assertThat(b.getAuthor().getFullName()).isNotBlank();
            assertThat(b.getGenres()).isNotEmpty();
        });
    }

    @Test
    @DisplayName("should delete existing book")
    void deleteExisting() {
        var book = persistBook("Deleted", "Author Delete", "Genre 1");
        entityManager.flush();
        entityManager.clear();

        repository.deleteById(book.getId());
        entityManager.flush();
        entityManager.clear();
        assertThat(entityManager.find(Book.class, book.getId())).isNull();
    }

    private Book persistBook(String title, String authorName, String... genreNames) {
        var author = entityManager.persist(new Author(null, authorName));
        var genres = new ArrayList<Genre>();
        for (String name : genreNames) {
            genres.add(entityManager.persist(new Genre(null, name)));
        }

        var book = new Book(null, title, author, new ArrayList<>());
        book.getGenres().addAll(genres);
        return entityManager.persistAndFlush(book);
    }
}
