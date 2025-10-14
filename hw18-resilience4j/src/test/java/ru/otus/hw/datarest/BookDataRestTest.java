package ru.otus.hw.datarest;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookDataRestTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CommentRepository commentRepository;

    private Author author;
    private Genre fantasy;
    private Genre adventure;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        bookRepository.deleteAll();
        genreRepository.deleteAll();
        authorRepository.deleteAll();

        author = saveAuthor("R. Martin");
        fantasy = saveGenre("Fantasy");
        adventure = saveGenre("Adventure");
    }

    @Test
    @DisplayName("should return books collection with HAL structure")
    void shouldReturnBooksCollection() {
        saveBook("Clean Code", author, List.of(fantasy, adventure));

        ResponseEntity<String> response = restTemplate.getForEntity(booksUrl(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext json = JsonPath.parse(response.getBody());
        Number totalElements = json.read("$.page.totalElements", Number.class);
        assertThat(totalElements.intValue()).isEqualTo(1);
        assertThat(json.read("$._embedded.books[0].title", String.class)).isEqualTo("Clean Code");
        assertThat(json.read("$._embedded.books[0]._links.self.href", String.class))
                .contains("/datarest/books/");
    }

    @Test
    @DisplayName("should return single book resource")
    void shouldReturnSingleBookResource() {
        Book persisted = saveBook("Kotlin in Action", author, List.of(fantasy));

        ResponseEntity<String> response = restTemplate.getForEntity(bookUrl(persisted.getId()), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext json = JsonPath.parse(response.getBody());
        assertThat(json.read("$.title", String.class)).isEqualTo("Kotlin in Action");
        assertThat(json.read("$._links.self.href", String.class))
                .endsWith("/datarest/books/" + persisted.getId());
        assertThat(json.read("$._links.author.href", String.class))
                .endsWith("/datarest/books/" + persisted.getId() + "/author");
    }

    @Test
    @DisplayName("should create book through REST endpoint")
    void shouldCreateBookThroughRestEndpoint() {
        HttpHeaders headers = halJsonHeaders();

        String payload = """
                {
                  "title": "Unfinished Tales",
                  "author": "%s",
                  "genres": ["%s", "%s"]
                }
                """.formatted(authorUri(author.getId()), genreUri(fantasy.getId()), genreUri(adventure.getId()));

        ResponseEntity<String> response = restTemplate.postForEntity(
                booksUrl(), new HttpEntity<>(payload, headers), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();

    URI location = Objects.requireNonNull(response.getHeaders().getLocation(), "Location header must be present");
    ResponseEntity<String> fetchResponse = restTemplate.getForEntity(location, String.class);

        assertThat(fetchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext json = JsonPath.parse(fetchResponse.getBody());
        assertThat(json.read("$.title", String.class)).isEqualTo("Unfinished Tales");
        assertThat(json.read("$._links.genres.href", String.class)).contains("/datarest/books/");
        assertThat(bookRepository.count()).isEqualTo(1);
    }

    private String booksUrl() {
        return baseUrl("/datarest/books");
    }

    private String bookUrl(Long id) {
        return booksUrl() + "/" + id;
    }

    private String authorUri(Long id) {
        return baseUrl("/datarest/authors/" + id);
    }

    private String genreUri(Long id) {
        return baseUrl("/datarest/genres/" + id);
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpHeaders halJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.parseMediaType("application/hal+json")));
        return headers;
    }

    private Author saveAuthor(String fullName) {
        return authorRepository.save(new Author(null, fullName));
    }

    private Genre saveGenre(String name) {
        return genreRepository.save(new Genre(null, name));
    }

    private Book saveBook(String title, Author bookAuthor, List<Genre> genres) {
        Book book = new Book(null, title, bookAuthor, new ArrayList<>(genres));
        return bookRepository.saveAndFlush(book);
    }
}
