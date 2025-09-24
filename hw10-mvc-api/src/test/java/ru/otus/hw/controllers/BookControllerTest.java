package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest
class BookControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private AuthorService authorService;

    @MockitoBean
    private GenreService genreService;

    @MockitoBean
    private CommentService commentService;

    @Test
    @DisplayName("GET /books returns list view with books")
    void listBooks() throws Exception {
        when(bookService.findAll()).thenReturn(List.of(stubBook()));

        mvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(model().attributeExists("books"));
    }

    @Test
    @DisplayName("GET /books/{id} returns view page with comments")
    void viewPage() throws Exception {
        when(bookService.getById(1L)).thenReturn(stubBook());
        when(commentService.findAllByBookId(1L)).thenReturn(List.of());

        mvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/view"))
                .andExpect(model().attributeExists("book", "comments"));
    }

    @Test
    @DisplayName("GET /books/{id} -> 404 when not found")
    void viewNotFound() throws Exception {
        when(bookService.getById(1L)).thenThrow(new EntityNotFoundException("Book with id 1 not found"));

        mvc.perform(get("/books/1"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"));
    }

    @Test
    @DisplayName("GET /books/new provides form and refs")
    void newForm() throws Exception {
        when(authorService.findAll()).thenReturn(emptyList());
        when(genreService.findAll()).thenReturn(emptyList());

        mvc.perform(get("/books/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().attributeExists("form", "authors", "genres"));
    }

    @Test
    @DisplayName("POST /books creates book and redirects")
    void create() throws Exception {
        when(bookService.insert("T", 1L, Set.of(1L, 2L))).thenReturn(stubBook());

        mvc.perform(post("/books")
                        .param("title", "T")
                        .param("authorId", "1")
                        .param("genreIds", "1", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"));
    }

    @Test
    @DisplayName("POST /books returns form with errors on validation failure")
    void createValidationError() throws Exception {
        when(authorService.findAll()).thenReturn(emptyList());
        when(genreService.findAll()).thenReturn(emptyList());

        mvc.perform(post("/books")
                        .param("title", "")
                        .param("authorId", "")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().attributeHasFieldErrors("form", "title", "authorId", "genreIds"));
    }

    @Test
    @DisplayName("GET /books/{id}/edit provides filled form")
    void editForm() throws Exception {
        when(bookService.getById(1L)).thenReturn(stubBook());
        when(authorService.findAll()).thenReturn(emptyList());
        when(genreService.findAll()).thenReturn(emptyList());

        mvc.perform(get("/books/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().attributeExists("form", "authors", "genres"));
    }

    @Test
    @DisplayName("POST /books/{id} updates and redirects")
    void update() throws Exception {
        when(bookService.update(eq(1L), eq("T2"), eq(1L), eq(Set.of(1L)))).thenReturn(stubBook());

        mvc.perform(post("/books/1")
                        .param("id", "1")
                        .param("title", "T2")
                        .param("authorId", "1")
                        .param("genreIds", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"));
    }

    @Test
    @DisplayName("POST /books/{id}/delete removes and redirects to list")
    void deletePost() throws Exception {
        mvc.perform(post("/books/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));
        verify(bookService).deleteById(1L);
    }


    private Book stubBook() {
        var a = new Author(1L, "Author_1");
        var g1 = new Genre(1L, "Genre_1");
        var g2 = new Genre(2L, "Genre_2");
        return new Book(1L, "BookTitle_1", a, List.of(g1, g2));
    }
}
