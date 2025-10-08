package ru.otus.hw.controllers.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.otus.hw.dto.BookFormDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.BookService;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @MockitoBean
    BookService bookService;

    @Test
    @DisplayName("GET /api/v1/books returns list")
    void list() throws Exception {
        when(bookService.findAll()).thenReturn(List.of(stubBook()));
        mvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("BookTitle_1"))
                .andExpect(jsonPath("$[0].author.fullName").value("Author_1"));
    }

    @Test
    @DisplayName("GET /api/v1/books/{id} returns one")
    void getOne() throws Exception {
        when(bookService.getById(1L)).thenReturn(stubBook());
        mvc.perform(get("/api/v1/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.genres", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/v1/books/{id} -> 404")
    void getNotFound() throws Exception {
        when(bookService.getById(9L)).thenThrow(new EntityNotFoundException("Book with id 9 not found"));
        mvc.perform(get("/api/v1/books/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("title").value("Not Found"));
    }

    @Test
    @DisplayName("POST /api/v1/books creates and returns 201 + body")
    void create() throws Exception {
        when(bookService.insert(eq("T"), eq(1L), eq(Set.of(1L, 2L)))).thenReturn(stubBook());
        var req = new BookFormDto("T", 1L, Set.of(1L, 2L));
        mvc.perform(post("/api/v1/books")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, endsWith("/api/v1/books/1")))
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("POST /api/v1/books -> 400 on validation error")
    void createValidationError() throws Exception {
        var req = new BookFormDto("", null, Set.of());
        mvc.perform(post("/api/v1/books")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("title").value("Bad Request"));
    }

    @Test
    @DisplayName("PUT /api/v1/books/{id} updates")
    void update() throws Exception {
        when(bookService.update(eq(1L), eq("T2"), eq(1L), eq(Set.of(1L)))).thenReturn(stubBook());
        var req = new BookFormDto("T2", 1L, Set.of(1L));
        mvc.perform(put("/api/v1/books/1")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("PATCH /api/v1/books/{id} partial update")
    void patch() throws Exception {
        when(bookService.getById(1L)).thenReturn(stubBook());
        when(bookService.update(eq(1L), eq("X"), any(), any())).thenReturn(stubBook());
        mvc.perform(MockMvcRequestBuilders.patch("/api/v1/books/1")
                        .contentType(APPLICATION_JSON)
                        .content("{\"title\":\"X\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/books/{id} -> 204")
    void deleteOk() throws Exception {
        mvc.perform(delete("/api/v1/books/1"))
                .andExpect(status().isNoContent());
        verify(bookService).deleteById(1L);
    }

    @Test
    @DisplayName("PATCH /api/v1/books/{id} -> 404 when book missing")
    void patchNotFound() throws Exception {
        when(bookService.getById(999L)).thenThrow(new EntityNotFoundException("Book with id 999 not found"));
        mvc.perform(MockMvcRequestBuilders.patch("/api/v1/books/999")
                        .contentType(APPLICATION_JSON)
                        .content("{\"title\":\"X\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("title").value("Not Found"));
    }

    @Test
    @DisplayName("PATCH /api/v1/books/{id} -> 400 on blank title")
    void patchValidation() throws Exception {
        when(bookService.getById(1L)).thenReturn(stubBook());
        mvc.perform(MockMvcRequestBuilders.patch("/api/v1/books/1")
                        .contentType(APPLICATION_JSON)
                        .content("{\"title\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("title").value("Bad Request"));
    }

    @Test
    @DisplayName("PATCH /api/v1/books/{id} can change author and genres")
    void patchAllFields() throws Exception {
        when(bookService.getById(1L)).thenReturn(stubBook());
        when(bookService.update(eq(1L), eq("X"), eq(2L), eq(Set.of(3L,4L)))).thenReturn(stubBook());
        mvc.perform(MockMvcRequestBuilders.patch("/api/v1/books/1")
                        .contentType(APPLICATION_JSON)
                        .content("{\"title\":\"X\",\"authorId\":2,\"genreIds\":[3,4]}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/books -> 400 on malformed JSON")
    void createMalformedJson() throws Exception {
        mvc.perform(post("/api/v1/books")
                        .contentType(APPLICATION_JSON)
                        .content("{ bad json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("detail").value("Malformed JSON"));
    }

    @Test
    @DisplayName("PUT /api/v1/books/{id} -> 400 on validation error")
    void updateValidationError() throws Exception {
        var req = new BookFormDto(" ", null, Set.of());
        mvc.perform(put("/api/v1/books/1")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("title").value("Bad Request"));
    }

    @Test
    @DisplayName("GET /api/v1/books/{id} -> 400 on type mismatch")
    void getTypeMismatch() throws Exception {
        mvc.perform(get("/api/v1/books/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("detail").value("Invalid parameter"));
    }

    @Test
    @DisplayName("PUT /api/v1/books/{id} -> 404 when book does not exist")
    void updateNotFound() throws Exception {
        when(bookService.update(eq(999L), any(), any(), any()))
                .thenThrow(new EntityNotFoundException("Book with id 999 not found"));
        var req = new BookFormDto("T", 1L, Set.of(1L));
        mvc.perform(put("/api/v1/books/999")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("title").value("Not Found"));
    }

    private Book stubBook() {
        var a = new Author(1L, "Author_1");
        var g1 = new Genre(1L, "Genre_1");
        var g2 = new Genre(2L, "Genre_2");
        return new Book(1L, "BookTitle_1", a, List.of(g1, g2));
    }

}
