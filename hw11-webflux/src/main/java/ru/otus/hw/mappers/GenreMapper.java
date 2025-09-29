package ru.otus.hw.mappers;

import lombok.experimental.UtilityClass;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.models.Genre;

@UtilityClass
public class GenreMapper {
    public GenreDto toDto(Genre g) {
        return new GenreDto(g.getId(), g.getName());
    }
}