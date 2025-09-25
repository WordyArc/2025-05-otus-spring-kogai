package ru.otus.hw.controllers.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthorViewController {

    @GetMapping("/authors")
    public String list() {
        return "authors/list";
    }
}
