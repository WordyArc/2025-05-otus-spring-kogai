package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
    public Optional<Comment> findById(String id) {
        Optional<Comment> comment = commentRepository.findById(id);
        comment.ifPresent(this::attachBook);

        return comment;
    }

    @Override
    public List<Comment> findAllByBookId(String bookId) {
        List<Comment> comments = commentRepository.findAllByBookId(bookId);
        comments.forEach(this::attachBook);

        return comments;
    }

    @Override
    public Comment create(String bookId, String text) {
        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %s not found".formatted(bookId)));
        var saved = commentRepository.save(new Comment(null, text, book.getId(), LocalDateTime.now(), null));
        saved.setBook(book);
        
        return saved;
    }

    @Override
    public Comment update(String id, String newText) {
        var comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with id %s not found".formatted(id)));
        comment.setText(newText);
        var saved = commentRepository.save(comment);
        attachBook(saved);

        return saved;
    }

    @Override
    public void deleteById(String id) {
        commentRepository.deleteById(id);
    }

    private void attachBook(Comment comment) {
        bookRepository.findById(comment.getBookId()).ifPresent(comment::setBook);
    }
}
