package ru.otus.hw.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.otus.hw.dto.BookFormDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.mappers.BookMapper;
import ru.otus.hw.mappers.CommentMapper;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.Set;

@Controller
@RequiredArgsConstructor
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    private final AuthorService authorService;

    private final GenreService genreService;

    private final CommentService commentService;

    @GetMapping
    public String list(Model model) {
        var books = bookService.findAll().stream().map(BookMapper::toView).toList();
        model.addAttribute("books", books);
        return "books/list";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        var book = bookService.getById(id);
        var dto = BookMapper.toView(book);
        var comments = commentService.findAllByBookId(id).stream()
                .map(CommentMapper::toDto).toList();
        model.addAttribute("book", dto);
        model.addAttribute("comments", comments);
        return "books/view";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new BookFormDto(null, "", null, Set.of()));
        addRefs(model);
        return "books/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") BookFormDto form,
                         BindingResult binding, Model model) {
        if (binding.hasErrors()) {
            addRefs(model);
            return "books/form";
        }
        var saved = bookService.insert(form.title(), form.authorId(), form.genreIds());
        return "redirect:/books/" + saved.getId();
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var book = bookService.getById(id);
        model.addAttribute("form", BookMapper.toFormDto(book));
        addRefs(model);
        return "books/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") BookFormDto form,
                         BindingResult binding, Model model) {
        if (binding.hasErrors()) {
            addRefs(model);
            return "books/form";
        }
        var saved = bookService.update(id, form.title(), form.authorId(), form.genreIds());
        return "redirect:/books/" + saved.getId();
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        bookService.deleteById(id);
        return "redirect:/books";
    }

    private void addRefs(Model model) {
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());
    }
}
