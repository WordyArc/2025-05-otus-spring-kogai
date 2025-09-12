package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.repositories.JdbcAuthorRepository;
import ru.otus.hw.repositories.JdbcBookRepository;
import ru.otus.hw.repositories.JdbcGenreRepository;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import({
        BookServiceImpl.class,
        JdbcBookRepository.class,
        JdbcAuthorRepository.class,
        JdbcGenreRepository.class
})
class BookServiceImplTest {

    @Autowired
    private BookService bookService;

    @Test
    @DisplayName("insert should throw when author does not exist")
    void shouldThrowWhenAuthorNotFoundOnInsert() {
        long missingAuthorId = 777L;
        assertThatThrownBy(() -> bookService.insert("Title", missingAuthorId, Set.of(1L)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("insert should throw when any of genres does not exist")
    void shouldThrowWhenAnyGenreMissingOnInsert() {
        assertThatThrownBy(() -> bookService.insert("Title", 1L, Set.of(1L, 999L)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("update should throw when book id does not exist")
    void shouldThrowWhenUpdatingMissingBook() {
        long missingBookId = 9_999L;
        assertThatThrownBy(() -> bookService.update(missingBookId, "Edited", 1L, Set.of(1L, 2L)))
                .isInstanceOf(EntityNotFoundException.class);
    }

}
