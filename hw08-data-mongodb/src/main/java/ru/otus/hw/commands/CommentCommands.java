package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.converters.CommentConverter;
import ru.otus.hw.services.CommentService;

import java.util.stream.Collectors;

@ShellComponent
@RequiredArgsConstructor
public class CommentCommands {

    private final CommentService commentService;

    private final CommentConverter commentConverter;

    // cbid 68cfe36a977d5fd6bcf0eb79
    @ShellMethod(value = "Find comment by id", key = "cbid")
    public String findById(String id) {
        return commentService.findById(id)
                .map(commentConverter::commentToString)
                .orElse("Comment with id %s not found".formatted(id));
    }

    // cbook b1
    @ShellMethod(value = "Find comments by book id", key = "cbook")
    public String findAllByBookId(String bookId) {
        var list = commentService.findAllByBookId(bookId);
        if (list.isEmpty()) {
            return "No comments for book %s".formatted(bookId);
        }
        return list.stream()
                .map(commentConverter::commentToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    // cins b1 "Dragon book - classic in compilers"
    @ShellMethod(value = "Create comment for a book", key = "cins")
    public String create(String bookId, String text) {
        var saved = commentService.create(bookId, text);
        return commentConverter.commentToString(saved);
    }

    // cupd b7 "Edited text"
    @ShellMethod(value = "Update comment text", key = "cupd")
    public String update(String id, String newText) {
        var updated = commentService.update(id, newText);
        return commentConverter.commentToString(updated);
    }

    // cdel b7
    @ShellMethod(value = "Delete comment by id", key = "cdel")
    public void delete(String id) {
        commentService.deleteById(id);
    }
}
