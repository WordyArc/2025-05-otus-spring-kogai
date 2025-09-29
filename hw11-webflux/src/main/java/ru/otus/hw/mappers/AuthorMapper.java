package ru.otus.hw.mappers;

import lombok.experimental.UtilityClass;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.models.Author;

@UtilityClass
public class AuthorMapper {
    public AuthorDto toDto(Author a) {
        return new AuthorDto(a.getId(), a.getFullName());
    }
}