package ru.otus.hw.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;
import ru.otus.hw.CommonContext;
import ru.otus.hw.TestData;
import ru.otus.hw.exceptions.EntityNotFoundException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class BookServiceTest extends CommonContext {

    @Autowired
    BookService bookService;

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
    @DisplayName("getById returns book with author & genres")
    void findById() {
        bookService.getById(1L)
                .as(StepVerifier::create)
                .assertNext(b -> {
                    assertThat(b.getAuthor().getFullName()).isEqualTo("Author_1");
                    assertThat(b.getGenres()).isNotEmpty();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("findAll returns books with relations")
    void findAll() {
        bookService.findAll().collectList()
                .as(StepVerifier::create)
                .assertNext(list -> {
                    assertThat(list).hasSize(3);
                    list.forEach(b -> {
                        assertThat(b.getAuthor().getFullName()).isNotBlank();
                        assertThat(b.getGenres()).isNotEmpty();
                    });
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("insert should error when author does not exist")
    void insertMissingAuthor() {
        bookService.insert("T", 777L, Set.of(1L))
                .as(StepVerifier::create)
                .expectErrorSatisfies(e -> assertThat(e)
                        .isInstanceOf(EntityNotFoundException.class)
                        .hasMessageContaining("Author with id 777 not found"))
                .verify();
    }

    @Test
    @DisplayName("insert should error when any genre does not exist")
    void insertMissingGenre() {
        bookService.insert("T", 1L, Set.of(1L, 999L))
                .as(StepVerifier::create)
                .expectErrorSatisfies(e -> assertThat(e)
                        .isInstanceOf(EntityNotFoundException.class)
                        .hasMessageContaining("One or all genres"))
                .verify();
    }

    @Test
    @DisplayName("update should error when book id does not exist")
    void updateMissingBook() {
        bookService.update(9999L, "Edited", 1L, Set.of(1L,2L))
                .as(StepVerifier::create)
                .expectErrorSatisfies(e -> assertThat(e)
                        .isInstanceOf(EntityNotFoundException.class)
                        .hasMessageContaining("Book with id 9999 not found"))
                .verify();
    }

    @Test
    @DisplayName("insert should error on empty genres set")
    void insertEmptyGenres() {
        bookService.insert("T", 1L, Set.of())
                .as(StepVerifier::create)
                .expectErrorSatisfies(e -> assertThat(e)
                        .isInstanceOf(IllegalArgumentException.class))
                .verify();
    }

}
