package ru.otus.hw.jpa.services;

import ru.otus.hw.jpa.models.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentService {
    Optional<Comment> findById(Long id);

    List<Comment> findAllByBookId(Long bookId);

    Comment create(Long bookId, String text);

    Comment update(Long id, String newText);

    void deleteById(Long id);
}
