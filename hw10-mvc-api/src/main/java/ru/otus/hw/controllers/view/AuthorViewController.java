package ru.otus.hw.controllers.view;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AuthorViewController {

    @GetMapping("/authors")
    public String list() {
        return "authors/list";
    }
}
