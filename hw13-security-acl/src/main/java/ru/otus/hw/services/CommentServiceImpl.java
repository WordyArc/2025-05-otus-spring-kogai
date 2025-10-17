package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Comment> findById(Long id) {
        return commentRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasPermission(#bookId, 'ru.otus.hw.models.Book', 'READ')")
    public List<Comment> findAllByBookId(Long bookId) {
        return commentRepository.findAllByBookId(bookId);
    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(#bookId, 'ru.otus.hw.models.Book', 'WRITE')")
    public Comment create(Long bookId, String text) {
        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(bookId)));
        return commentRepository.save(new Comment(null, text, book, LocalDateTime.now()));
    }

    @Override
    @Transactional
    @PreAuthorize("@acl.canUpdateComment(#id)")
    public Comment update(Long id, String newText) {
        var comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with id %d not found".formatted(id)));
        comment.setText(newText);
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    @PreAuthorize("@acl.canDeleteComment(#id)")
    public void deleteById(Long id) {
        commentRepository.deleteById(id);
    }
}
