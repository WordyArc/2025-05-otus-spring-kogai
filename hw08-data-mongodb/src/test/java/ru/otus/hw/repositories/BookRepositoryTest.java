package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import ru.otus.hw.CommonContext;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class BookRepositoryTest extends CommonContext {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        genreRepository.deleteAll();

        var a1 = authorRepository.save(new Author("a1", "Author_1"));
        var a2 = authorRepository.save(new Author("a2", "Author_2"));
        var a3 = authorRepository.save(new Author("a3", "Author_3"));

        var g1 = genreRepository.save(new Genre("g1", "Genre_1"));
        var g2 = genreRepository.save(new Genre("g2", "Genre_2"));
        var g3 = genreRepository.save(new Genre("g3", "Genre_3"));
        var g4 = genreRepository.save(new Genre("g4", "Genre_4"));
        var g5 = genreRepository.save(new Genre("g5", "Genre_5"));
        var g6 = genreRepository.save(new Genre("g6", "Genre_6"));

        bookRepository.save(new Book("b1", "BookTitle_1", a1, List.of(g1, g2)));
        bookRepository.save(new Book("b2", "BookTitle_2", a2, List.of(g3, g4)));
        bookRepository.save(new Book("b3", "BookTitle_3", a3, List.of(g5, g6)));
    }

    @Test
    @DisplayName("should find book by id with author and genres")
    void shouldFindById() {
        var book = bookRepository.findById("b1").orElseThrow();
        assertThat(book.getTitle()).isEqualTo("BookTitle_1");
        assertThat(book.getAuthor().getFullName()).isEqualTo("Author_1");
        assertThat(book.getGenres()).hasSize(2);
    }

    @Test
    @DisplayName("should return all books with author and genres")
    void shouldFindAll() {
        var list = bookRepository.findAll();
        assertThat(list).hasSize(3);
        list.forEach(b -> {
            assertThat(b.getAuthor().getFullName()).isNotBlank();
            assertThat(b.getGenres()).isNotEmpty();
        });
    }

    @Test
    @DisplayName("should delete existing book")
    void deleteExisting() {
        bookRepository.deleteById("b1");
        assertThat(bookRepository.findById("b1")).isEmpty();
    }

}
