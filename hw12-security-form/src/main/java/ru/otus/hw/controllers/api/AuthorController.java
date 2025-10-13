package ru.otus.hw.controllers.api;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.services.AuthorService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/authors")
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping
    public List<AuthorDto> authors() {
        return authorService.findAll().stream()
                .map(a -> new AuthorDto(a.getId(), a.getFullName()))
                .toList();
    }

}
