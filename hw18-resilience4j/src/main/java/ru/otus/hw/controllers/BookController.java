package ru.otus.hw.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookFormDto;
import ru.otus.hw.mappers.BookMapper;
import ru.otus.hw.services.BookService;
import ru.otus.hw.util.PatchUtils;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookService bookService;

    @GetMapping
    public List<BookDto> list() {
        return bookService.findAll().stream().map(BookMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public BookDto get(@PathVariable Long id) {
        return BookMapper.toDto(bookService.getById(id));
    }

    @PostMapping
    public ResponseEntity<BookDto> create(@RequestBody @Valid BookFormDto formDto, UriComponentsBuilder uriBuilder) {
        var saved = bookService.insert(formDto.title(), formDto.authorId(), formDto.genreIds());
        var dto = BookMapper.toDto(saved);
        var location = uriBuilder
                .path("/api/v1/books/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(location).body(dto);
    }

    @PutMapping("/{id}")
    public BookDto update(@PathVariable Long id, @Valid @RequestBody BookFormDto dto) {
        var saved = bookService.update(id, dto.title(), dto.authorId(), dto.genreIds());
        return BookMapper.toDto(saved);
    }

    @PatchMapping("/{id}")
    public BookDto patch(@PathVariable Long id, @RequestBody Map<String, Object> patch) {
        var book = bookService.getById(id);
        var args = PatchUtils.mergeBookPatch(patch, book);
        var saved = bookService.update(id, args.title(), args.authorId(), args.genreIds());
        return BookMapper.toDto(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
