package ru.otus.hw.controllers.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = BookViewController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookViewControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("GET /books -> books/list")
    void list() throws Exception {
        mvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"));
    }

    @Test
    @DisplayName("GET /books/new -> books/form")
    void newForm() throws Exception {
        mvc.perform(get("/books/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"));
    }

    @Test
    @DisplayName("GET /books/{id} -> books/view")
    void viewPage() throws Exception {
        mvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/view"));
    }

    @Test
    @DisplayName("GET /books/{id}/edit -> books/form")
    void editForm() throws Exception {
        mvc.perform(get("/books/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"));
    }
}
