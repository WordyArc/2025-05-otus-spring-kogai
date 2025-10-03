package ru.otus.hw.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.config.SecurityConfig;
import ru.otus.hw.controllers.api.AuthorController;
import ru.otus.hw.controllers.api.BookController;
import ru.otus.hw.controllers.api.CommentController;
import ru.otus.hw.controllers.api.GenreController;
import ru.otus.hw.dto.BookFormDto;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.CustomUserDetailsService;
import ru.otus.hw.services.GenreService;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        BookController.class,
        AuthorController.class,
        GenreController.class,
        CommentController.class
})
@Import(SecurityConfig.class)
class ApiSecurityTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;
    @MockitoBean
    private BookService bookService;
    @MockitoBean
    private AuthorService authorService;
    @MockitoBean
    private GenreService genreService;
    @MockitoBean
    private CommentService commentService;

    @Nested
    class UnauthenticatedUser {

        @Test
        @DisplayName("should deny reading resources")
        void shouldDenyRead() throws Exception {
            mvc.perform(get("/api/v1/books"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should deny creating resources")
        void shouldDenyCreate() throws Exception {
            var bookForm = new BookFormDto("Title", 1L, Set.of(1L));

            mvc.perform(post("/api/v1/books")
                            .contentType(APPLICATION_JSON)
                            .content(mapper.writeValueAsString(bookForm)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should deny updating resources")
        void shouldDenyUpdate() throws Exception {
            var bookForm = new BookFormDto("Title", 1L, Set.of(1L));

            mvc.perform(put("/api/v1/books/1")
                            .contentType(APPLICATION_JSON)
                            .content(mapper.writeValueAsString(bookForm)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should deny deleting resources")
        void shouldDenyDelete() throws Exception {
            mvc.perform(delete("/api/v1/books/1"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("when user is authenticated")
    class AuthenticatedUser {

        @Test
        @DisplayName("should allow reading resources")
        void shouldAllowRead() throws Exception {
            when(bookService.findAll()).thenReturn(Collections.emptyList());

            mvc.perform(get("/api/v1/books")
                            .with(user("testUser")))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should allow creating resources")
        void shouldAllowCreate() throws Exception {
            stubBook();
            var bookForm = new BookFormDto("New Book", 1L, Set.of(1L));

            mvc.perform(post("/api/v1/books")
                            .with(user("testUser"))
                            .contentType(APPLICATION_JSON)
                            .content(mapper.writeValueAsString(bookForm)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("should allow updating resources")
        void shouldAllowUpdate() throws Exception {
            stubBookUpdate();
            var bookForm = new BookFormDto("Updated Book", 1L, Set.of(1L));

            mvc.perform(put("/api/v1/books/1")
                            .with(user("testUser"))
                            .contentType(APPLICATION_JSON)
                            .content(mapper.writeValueAsString(bookForm)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should allow deleting resources")
        void shouldAllowDelete() throws Exception {
            mvc.perform(delete("/api/v1/books/1")
                            .with(user("testUser")))
                    .andExpect(status().isNoContent());
        }
    }

    private Book stubBook() {
        var author = new Author(1L, "Author");
        var genre = new Genre(1L, "Genre");
        var book = new Book(1L, "Book", author, List.of(genre));
        when(bookService.insert(anyString(), anyLong(), anySet())).thenReturn(book);
        return book;
    }

    private Book stubBookUpdate() {
        var author = new Author(1L, "Author");
        var genre = new Genre(1L, "Genre");
        var book = new Book(1L, "Book", author, List.of(genre));
        when(bookService.update(anyLong(), anyString(), anyLong(), anySet())).thenReturn(book);
        return book;
    }
}
