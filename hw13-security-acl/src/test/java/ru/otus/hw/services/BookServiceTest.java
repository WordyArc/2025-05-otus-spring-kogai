package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private AclBookService aclBookService;

    @InjectMocks
    private BookServiceImpl bookService;

    private Author testAuthor;
    private Genre genre1;
    private Genre genre2;
    private Book testBook;

    @BeforeEach
    void setUp() {
        testAuthor = new Author(1L, "Test Author");
        genre1 = new Genre(1L, "Genre 1");
        genre2 = new Genre(2L, "Genre 2");
        testBook = new Book(1L, "Test Book", testAuthor, List.of(genre1, genre2));
    }

    @Test
    @DisplayName("should find book by id with all relations")
    void shouldFindById() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        var result = bookService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Test Book");
        assertThat(result.get().getAuthor().getFullName()).isEqualTo("Test Author");
        assertThat(result.get().getGenres()).hasSize(2);
    }

    @Test
    @DisplayName("should find all books with relations")
    void shouldFindAll() {
        var book2 = new Book(2L, "Another Book", testAuthor, List.of(genre1));
        when(bookRepository.findAll()).thenReturn(List.of(testBook, book2));

        var result = bookService.findAll();

        assertThat(result).hasSize(2);
        result.forEach(book -> {
            assertThat(book.getAuthor()).isNotNull();
            assertThat(book.getGenres()).isNotEmpty();
        });
    }

    @Test
    @DisplayName("should successfully insert book with valid data")
    void shouldInsertBook() {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
        when(genreRepository.findAllByIdIn(Set.of(1L, 2L))).thenReturn(List.of(genre1, genre2));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        var result = bookService.insert("New Book", 1L, Set.of(1L, 2L));

        assertThat(result).isNotNull();
        verify(bookRepository).save(any(Book.class));
        verify(aclBookService).createDefaultAcl(testBook);
    }

    @Test
    @DisplayName("should throw when inserting with non-existent author")
    void shouldThrowWhenInsertingWithMissingAuthor() {
        when(authorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.insert("Book", 999L, Set.of(1L)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Author with id 999 not found");
    }

    @Test
    @DisplayName("should throw when inserting with non-existent genre")
    void shouldThrowWhenInsertingWithMissingGenre() {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
        when(genreRepository.findAllByIdIn(Set.of(1L, 999L))).thenReturn(List.of(genre1));

        assertThatThrownBy(() -> bookService.insert("Book", 1L, Set.of(1L, 999L)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("One or all genres");
    }

    @Test
    @DisplayName("should throw when inserting with empty genres")
    void shouldThrowWhenInsertingWithEmptyGenres() {
        assertThatThrownBy(() -> bookService.insert("Book", 1L, Set.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Genres ids must not be null or empty");
    }

    @Test
    @DisplayName("should successfully update existing book")
    void shouldUpdateBook() {
        when(bookRepository.existsById(1L)).thenReturn(true);
        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
        when(genreRepository.findAllByIdIn(Set.of(1L))).thenReturn(List.of(genre1));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        var result = bookService.update(1L, "Updated Title", 1L, Set.of(1L));

        assertThat(result).isNotNull();
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @DisplayName("should throw when updating non-existent book")
    void shouldThrowWhenUpdatingMissingBook() {
        when(bookRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> bookService.update(999L, "Title", 1L, Set.of(1L)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book with id 999 not found");
    }

    @Test
    @DisplayName("should delete book by id")
    void shouldDeleteById() {
        bookService.deleteById(1L);

        verify(bookRepository).deleteById(1L);
        verify(aclBookService).deleteAcl(1L);
    }
}
