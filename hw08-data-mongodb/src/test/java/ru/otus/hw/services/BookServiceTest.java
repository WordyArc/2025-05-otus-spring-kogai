package ru.otus.hw.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.CommonContext;
import ru.otus.hw.TestData;
import ru.otus.hw.config.TestDataConfig;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.CommentRepository;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@Import(TestDataConfig.class)
class BookServiceTest extends CommonContext {

    @Autowired
    private BookService bookService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    protected TestData data;

    @BeforeEach
    void setUp() {
        data.resetAndSeed();
    }

    @AfterEach
    void tearDown() {
        data.cleanAll();
    }

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

    @Test
    @DisplayName("insert creates book with existing author & genres")
    void insertSuccess() {
        var saved = bookService.insert("New Book", "a1", Set.of("g1", "g2"));
        assertThat(saved.getId()).isNotBlank();
        var reloaded = bookService.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getTitle()).isEqualTo("New Book");
        assertThat(reloaded.getAuthor().getId()).isEqualTo("a1");
        assertThat(reloaded.getGenres()).extracting(Genre::getId).containsExactlyInAnyOrder("g1","g2");
    }

    @Test
    @DisplayName("update changes title/author/genres for existing book")
    void updateSuccess() {
        var updated = bookService.update("b1", "Edited", "a2", Set.of("g3", "g4"));
        assertThat(updated.getTitle()).isEqualTo("Edited");
        assertThat(updated.getAuthor().getId()).isEqualTo("a2");
        assertThat(updated.getGenres()).extracting(Genre::getId)
                .containsExactlyInAnyOrder("g3", "g4");

        var reloaded = bookService.findById("b1").orElseThrow();
        assertThat(updated.getId()).isEqualTo("b1");
        assertThat(reloaded.getTitle()).isEqualTo("Edited");
        assertThat(reloaded.getAuthor().getId()).isEqualTo("a2");
        assertThat(reloaded.getGenres()).extracting(Genre::getId).containsExactlyInAnyOrder("g3","g4");
    }

    @Test
    @DisplayName("deleteById removes the book and its comments")
    void deleteCascadesComments() {
        commentRepository.save(new Comment(null, "temp", "b1", LocalDateTime.now(), null));
        assertThat(commentRepository.findAllByBookId("b1")).hasSize(1);

        bookService.deleteById("b1");

        assertThat(bookService.findById("b1")).isEmpty();
        assertThat(commentRepository.findAllByBookId("b1")).isEmpty();
    }

    @Test
    @DisplayName("findById returns empty for missing book")
    void findByIdMissing() {
        assertThat(bookService.findById("nope")).isEmpty();
    }

    @Test
    @DisplayName("deleteById is idempotent for missing book")
    void deleteIdempotent() {
        var before = bookService.findAll().size();
        bookService.deleteById("missing");
        var after = bookService.findAll().size();
        assertThat(after).isEqualTo(before);
    }

}
