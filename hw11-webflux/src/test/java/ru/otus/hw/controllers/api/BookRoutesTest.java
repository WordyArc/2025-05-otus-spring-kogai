package ru.otus.hw.controllers.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.controllers.api.handlers.AuthorHandler;
import ru.otus.hw.controllers.api.handlers.BookHandler;
import ru.otus.hw.controllers.api.handlers.CommentHandler;
import ru.otus.hw.controllers.api.handlers.GenreHandler;
import ru.otus.hw.dto.BookFormDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.exceptions.ErrorHandling;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebFluxTest
@Import(value = {
        FunctionalEndpointsConfig.class,
        BookHandler.class, AuthorHandler.class, GenreHandler.class, CommentHandler.class,
        ErrorHandling.class
})
class BookRoutesTest {

    @Autowired
    WebTestClient web;

    @MockitoBean
    BookService bookService;

    @MockitoBean
    AuthorService authorService;

    @MockitoBean
    GenreService genreService;

    @MockitoBean
    CommentService commentService;

    @Test
    @DisplayName("GET /api/v1/books returns list")
    void list() {
        given(bookService.findAll()).willReturn(Flux.just(stubBook()));

        web.get().uri("/api/v1/books")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$").value(hasSize(1))
                .jsonPath("$[0].title").isEqualTo("BookTitle_1")
                .jsonPath("$[0].author.fullName").isEqualTo("Author_1");
    }

    @Test
    @DisplayName("GET /api/v1/books/{id} returns one")
    void getOne() {
        given(bookService.getById(1L)).willReturn(Mono.just(stubBook()));

        web.get().uri("/api/v1/books/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.genres").value(hasSize(2));
    }

    @Test
    @DisplayName("GET /api/v1/books/{id} -> 404")
    void getNotFound() {
        given(bookService.getById(9L))
                .willReturn(Mono.error(new EntityNotFoundException("Book with id 9 not found")));

        web.get().uri("/api/v1/books/9")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().jsonPath("$.title").isEqualTo("Not Found");
    }

    @Test
    @DisplayName("GET /api/v1/books/{id} -> 400 on type mismatch")
    void getTypeMismatch() {
        web.get().uri("/api/v1/books/abc")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.title").isEqualTo("Bad Request");
    }

    @Test
    @DisplayName("POST /api/v1/books creates 201 + body")
    void create() {
        given(bookService.insert(eq("T"), eq(1L), eq(Set.of(1L, 2L))))
                .willReturn(Mono.just(stubBook()));
        var req = new BookFormDto("T", 1L, Set.of(1L, 2L));

        web.post().uri("/api/v1/books")
                .contentType(APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().value(HttpHeaders.LOCATION, v -> assertThat(v).endsWith("/api/v1/books/1"))
                .expectBody().jsonPath("$.id").isEqualTo(1);
    }

    @Test
    @DisplayName("POST /api/v1/books -> 400 on validation error")
    void createValidationError() {
        var req = new BookFormDto("", null, Set.of());

        web.post().uri("/api/v1/books")
                .contentType(APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.title").isEqualTo("Bad Request");
    }

    @Test
    @DisplayName("POST /api/v1/books -> 400 on malformed JSON")
    void createMalformedJson() {
        web.post().uri("/api/v1/books")
                .contentType(APPLICATION_JSON)
                .bodyValue("{ bad json")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.detail").isEqualTo("Malformed JSON");
    }

    @Test
    @DisplayName("PUT /api/v1/books/{id} updates")
    void update() {
        given(bookService.update(eq(1L), eq("T2"), eq(1L), eq(Set.of(1L))))
                .willReturn(Mono.just(stubBook()));
        var req = new BookFormDto("T2", 1L, Set.of(1L));

        web.put().uri("/api/v1/books/1")
                .contentType(APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.id").isEqualTo(1);
    }

    @Test
    @DisplayName("PUT /api/v1/books/{id} -> 400 on validation error")
    void updateValidationError() {
        var req = new BookFormDto(" ", null, Set.of());

        web.put().uri("/api/v1/books/1")
                .contentType(APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.title").isEqualTo("Bad Request");
    }

    @Test
    @DisplayName("PUT /api/v1/books/{id} -> 404 when book not found")
    void updateNotFound() {
        given(bookService.update(eq(999L), anyString(), anyLong(), anySet()))
                .willReturn(Mono.error(new EntityNotFoundException("Book with id 999 not found")));
        var req = new BookFormDto("T", 1L, Set.of(1L));

        web.put().uri("/api/v1/books/999")
                .contentType(APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().jsonPath("$.title").isEqualTo("Not Found");
    }

    @Test
    @DisplayName("PATCH /api/v1/books/{id} partial update")
    void patch() {
        given(bookService.getById(1L)).willReturn(Mono.just(stubBook()));
        given(bookService.update(eq(1L), eq("X"), anyLong(), anySet()))
                .willReturn(Mono.just(stubBook()));

        web.patch().uri("/api/v1/books/1")
                .contentType(APPLICATION_JSON)
                .bodyValue("{\"title\":\"X\"}")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("PATCH /api/v1/books/{id} -> 400 on blank title")
    void patchValidation() {
        given(bookService.getById(1L)).willReturn(Mono.just(stubBook()));

        web.patch().uri("/api/v1/books/1")
                .contentType(APPLICATION_JSON)
                .bodyValue("{\"title\":\"   \"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.title").isEqualTo("Bad Request");
    }

    @Test
    @DisplayName("PATCH /api/v1/books/{id} -> 404 when book missing")
    void patchNotFound() {
        given(bookService.getById(999L))
                .willReturn(Mono.error(new EntityNotFoundException("Book with id 999 not found")));

        web.patch().uri("/api/v1/books/999")
                .contentType(APPLICATION_JSON)
                .bodyValue("{\"title\":\"X\"}")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().jsonPath("$.title").isEqualTo("Not Found");
    }

    @Test
    @DisplayName("DELETE /api/v1/books/{id} -> 204")
    void deleteOk() {
        given(bookService.deleteById(1L)).willReturn(Mono.empty());

        web.delete().uri("/api/v1/books/1")
                .exchange()
                .expectStatus().isNoContent();
    }

    private Book stubBook() {
        var a = new Author(1L, "Author_1");
        var g1 = new Genre(1L, "Genre_1");
        var g2 = new Genre(2L, "Genre_2");
        return new Book(1L, "BookTitle_1", 1L, a, List.of(g1, g2));
    }
}
