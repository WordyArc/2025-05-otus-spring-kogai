package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record BookFormDto(
        Long id,

        @NotBlank(message = "Title must not be blank")
        String title,

        @NotNull(message = "Author is required")
        Long authorId,

        @NotEmpty(message = "Select at least one genre")
        Set<Long> genreIds
) {
}