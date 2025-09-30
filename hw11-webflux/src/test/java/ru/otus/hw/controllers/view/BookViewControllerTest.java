package ru.otus.hw.controllers.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.http.MediaType.TEXT_HTML;

@WebFluxTest(controllers = BookViewController.class)
class BookViewControllerTest {

    @Autowired
    WebTestClient client;

    @Test
    @DisplayName("GET /books -> 200 text/html")
    void list() {
        client.get().uri("/books")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(TEXT_HTML);
    }

    @Test
    @DisplayName("GET /books/new -> 200 text/html")
    void newForm() {
        client.get().uri("/books/new")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(TEXT_HTML);
    }

    @Test
    @DisplayName("GET /books/{id} -> 200 text/html")
    void viewPage() {
        client.get().uri("/books/1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(TEXT_HTML);
    }

    @Test
    @DisplayName("GET /books/{id}/edit -> 200 text/html")
    void editForm() {
        client.get().uri("/books/1/edit")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(TEXT_HTML);
    }
}
