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
import ru.otus.hw.models.Comment;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.time.LocalDateTime;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebFluxTest
@Import(value = {
        FunctionalEndpointsConfig.class,
        BookHandler.class, AuthorHandler.class, GenreHandler.class, CommentHandler.class,
        ErrorHandling.class
})
public class CommentRoutesTest {

    @Autowired
    WebTestClient web;

    @MockitoBean
    CommentService commentService;

    @MockitoBean
    BookService bookService;

    @MockitoBean
    AuthorService authorService;

    @MockitoBean
    GenreService genreService;

    @Test
    @DisplayName("GET /api/v1/books/{id}/comments returns list")
    void list() {
        var c = new Comment(1L, "Nice", 1L, LocalDateTime.now());
        given(commentService.findAllByBookId(1L)).willReturn(Flux.just(c));

        web.get().uri("/api/v1/books/1/comments")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].text").isEqualTo("Nice");
    }

}
