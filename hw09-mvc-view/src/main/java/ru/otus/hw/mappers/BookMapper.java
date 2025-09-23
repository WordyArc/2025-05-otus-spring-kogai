package ru.otus.hw.mappers;

import lombok.experimental.UtilityClass;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookFormDto;
import ru.otus.hw.dto.BookView;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class BookMapper {

    public BookView toView(Book b) {
        var authorDto = new AuthorDto(b.getAuthor().getId(), b.getAuthor().getFullName());
        var genres = b.getGenres().stream()
                .map(g -> new GenreDto(g.getId(), g.getName()))
                .toList();
        return new BookView(b.getId(), b.getTitle(), authorDto, genres);
    }

    public BookFormDto toFormDto(Book b) {
        Set<Long> gIds = b.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());
        return new BookFormDto(b.getId(), b.getTitle(), b.getAuthor().getId(), gIds);
    }

}
