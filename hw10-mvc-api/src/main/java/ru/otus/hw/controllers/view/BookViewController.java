package ru.otus.hw.controllers.view;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/books")
public class BookViewController {

    @GetMapping
    public String list() {
        return "books/list";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id) {
        return "books/view";
    }

    @GetMapping("/new")
    public String createForm() {
        return "books/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id) {
        return "books/form";
    }
}
