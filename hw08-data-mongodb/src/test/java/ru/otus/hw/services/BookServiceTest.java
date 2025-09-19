package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.otus.hw.exceptions.EntityNotFoundException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
class BookServiceTest {

    @Autowired
    private BookService bookService;

    @Test
    @DisplayName("findById returns book with accessible relations outside tx")
    void findById() {
        var book = bookService.findById(1L).orElseThrow();
        assertThat(book.getAuthor().getFullName()).isEqualTo("Author_1");
        assertThat(book.getGenres()).isNotEmpty();
    }

    @Test
    @DisplayName("findAll returns books with accessible relations")
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
        assertThatThrownBy(() -> bookService.insert("T", 777L, Set.of(1L)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Author with id 777 not found");
    }

    @Test
    @DisplayName("insert should throw when any genre does not exist")
    void insertMissingGenre() {
        assertThatThrownBy(() -> bookService.insert("T", 1L, Set.of(1L, 999L)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("One or all genres");
    }

    @Test
    @DisplayName("update should throw when book id does not exist")
    void updateMissingBook() {
        assertThatThrownBy(() -> bookService.update(9999L, "Edited", 1L, Set.of(1L,2L)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book with id 9999 not found");
    }

    @Test
    @DisplayName("insert should throw on empty genres set")
    void insertEmptyGenres() {
        assertThatThrownBy(() -> bookService.insert("T", 1L, Set.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
