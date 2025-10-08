package ru.otus.hw.controllers.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import ru.otus.hw.controllers.api.handlers.AuthorHandler;
import ru.otus.hw.controllers.api.handlers.BookHandler;
import ru.otus.hw.controllers.api.handlers.CommentHandler;
import ru.otus.hw.controllers.api.handlers.GenreHandler;
import ru.otus.hw.exceptions.ErrorHandling;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebFluxTest
@Import(value = {
        FunctionalEndpointsConfig.class,
        BookHandler.class, AuthorHandler.class, GenreHandler.class, CommentHandler.class,
        ErrorHandling.class
})
public class GenreRoutesTest {

    @Autowired
    WebTestClient web;

    @MockitoBean
    GenreService genreService;

    @MockitoBean
    BookService bookService;

    @MockitoBean
    AuthorService authorService;

    @MockitoBean
    CommentService commentService;

    @Test
    @DisplayName("GET /api/v1/genres returns list")
    void list() {
        given(genreService.findAll()).willReturn(Flux.just(new Genre(1L, "Genre_1")));

        web.get().uri("/api/v1/genres")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].name").isEqualTo("Genre_1");
    }

}
