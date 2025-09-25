package ru.otus.hw.controllers.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GenreViewController {

    @GetMapping("/genres")
    public String list() {
        return "genres/list";
    }
}
