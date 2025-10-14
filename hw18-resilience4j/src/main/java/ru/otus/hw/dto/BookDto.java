package ru.otus.hw.dto;

import java.util.List;

public record BookDto(Long id, String title, AuthorDto author, List<GenreDto> genres) {

}
