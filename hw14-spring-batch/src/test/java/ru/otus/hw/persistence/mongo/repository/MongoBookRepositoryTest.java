package ru.otus.hw.persistence.mongo.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import ru.otus.hw.persistence.mongo.model.AuthorDocument;
import ru.otus.hw.persistence.mongo.model.BookDocument;
import ru.otus.hw.persistence.mongo.model.GenreDocument;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class MongoBookRepositoryTest {

    @Autowired
    private MongoBookRepository bookRepository;

    @Autowired
    private MongoAuthorRepository authorRepository;

    @Autowired
    private MongoGenreRepository genreRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        genreRepository.deleteAll();

        var a1 = authorRepository.save(new AuthorDocument(null, "Author_1"));
        var a2 = authorRepository.save(new AuthorDocument(null, "Author_2"));
        var a3 = authorRepository.save(new AuthorDocument(null, "Author_3"));

        var g1 = genreRepository.save(new GenreDocument(null, "Genre_1"));
        var g2 = genreRepository.save(new GenreDocument(null, "Genre_2"));
        var g3 = genreRepository.save(new GenreDocument(null, "Genre_3"));
        var g4 = genreRepository.save(new GenreDocument(null, "Genre_4"));
        var g5 = genreRepository.save(new GenreDocument(null, "Genre_5"));
        var g6 = genreRepository.save(new GenreDocument(null, "Genre_6"));

        bookRepository.save(new BookDocument(null, "BookTitle_1", a1, List.of(g1, g2)));
        bookRepository.save(new BookDocument(null, "BookTitle_2", a2, List.of(g3, g4)));
        bookRepository.save(new BookDocument(null, "BookTitle_3", a3, List.of(g5, g6)));
    }

    @Test
    @DisplayName("should find book by id with author and genres")
    void shouldFindById() {
        var savedBook = bookRepository.findAll().stream()
                .filter(b -> "BookTitle_1".equals(b.getTitle()))
                .findFirst()
                .orElseThrow();
        
        var book = bookRepository.findById(savedBook.getId()).orElseThrow();
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
        var savedBook = bookRepository.findAll().stream()
                .filter(b -> "BookTitle_1".equals(b.getTitle()))
                .findFirst()
                .orElseThrow();
        
        bookRepository.deleteById(savedBook.getId());
        assertThat(bookRepository.findById(savedBook.getId())).isEmpty();
    }

}
