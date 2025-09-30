package ru.otus.hw.dto;

import java.util.List;
import java.util.stream.Collectors;

public record BookView(Long id, String title, AuthorDto author, List<GenreDto> genres) {
    public String joinGenres() {
        return genres == null ? "" : genres.stream()
                .map(GenreDto::name)
                .collect(Collectors.joining(", "));
    }
}
