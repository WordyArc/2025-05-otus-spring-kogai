package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
    public Mono<Comment> findById(Long id) {
        return commentRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<Comment> findAllByBookId(Long bookId) {
        return commentRepository.findAllByBookId(bookId);
    }

    @Override
    @Transactional
    public Mono<Comment> create(Long bookId, String text) {
        return bookRepository.existsById(bookId)
                .flatMap(exists -> exists
                        ? commentRepository.save(new Comment(null, text, bookId, LocalDateTime.now()))
                        : Mono.error(new EntityNotFoundException("Book with id %d not found".formatted(bookId))));
    }

    @Override
    @Transactional
    public Mono<Comment> update(Long id, String newText) {
        return commentRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Comment with id %d not found".formatted(id))))
                .flatMap(c -> {
                    c.setText(newText);
                    return commentRepository.save(c);
                });
    }

    @Override
    @Transactional
    public Mono<Void> deleteById(Long id) {
        return commentRepository.deleteById(id);
    }
}
