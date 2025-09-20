package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.MongoIntegrationTest;
import ru.otus.hw.TestDataConfig;
import ru.otus.hw.exceptions.EntityNotFoundException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@Import(TestDataConfig.class)
class BookServiceTest extends MongoIntegrationTest {

    @Autowired
    private BookService bookService;

    @Test
    @DisplayName("findById returns book with author & genres")
    void findById() {
        var book = bookService.findById("b1").orElseThrow();
        assertThat(book.getTitle()).isEqualTo("BookTitle_1");
        assertThat(book.getAuthor().getFullName()).isEqualTo("Author_1");
        assertThat(book.getGenres()).hasSize(2);
    }

    @Test
    @DisplayName("findAll returns books with relations")
    void findAll() {
        var books = bookService.findAll();
        assertThat(books).hasSize(3);
        books.forEach(b -> {
            assertThat(b.getAuthor().getFullName()).isNotBlank();
            assertThat(b.getGenres()).isNotEmpty();
        });
    }

    @Test
    @DisplayName("insert should throw when author does not exist")
    void insertMissingAuthor() {
        assertThatThrownBy(() -> bookService.insert("T", "no-author", Set.of("g1")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Author with id no-author not found");
    }

    @Test
    @DisplayName("insert should throw when any genre does not exist")
    void insertMissingGenre() {
        assertThatThrownBy(() -> bookService.insert("T", "a1", Set.of("g1", "bad")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("One or all genres");
    }

    @Test
    @DisplayName("update should throw when book id does not exist")
    void updateMissingBook() {
        assertThatThrownBy(() -> bookService.update("no-book", "Edited", "a1", Set.of("g1","g2")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book with id no-book not found");
    }

    @Test
    @DisplayName("insert should throw on empty genres set")
    void insertEmptyGenres() {
        assertThatThrownBy(() -> bookService.insert("T", "a1", Set.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
