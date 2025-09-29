package ru.otus.hw.mappers;

import lombok.experimental.UtilityClass;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.models.Book;

@UtilityClass
public class BookMapper {

    public BookDto toDto(Book b) {
        var authorDto = new AuthorDto(b.getAuthor().getId(), b.getAuthor().getFullName());
        var genres = b.getGenres().stream()
                .map(g -> new GenreDto(g.getId(), g.getName()))
                .toList();
        return new BookDto(b.getId(), b.getTitle(), authorDto, genres);
    }

//    public BookDto toDto(Book b) {
//        Author a = b.getAuthor();
//        AuthorDto authorDto = a == null ? null : new AuthorDto(a.getId(), a.getFullName());
//        List<GenreDto> genres = b.getGenres() == null ? List.of()
//                : b.getGenres().stream().map(g -> new GenreDto(g.getId(), g.getName())).toList();
//        return new BookDto(b.getId(), b.getTitle(), authorDto, genres);
//    }

}
