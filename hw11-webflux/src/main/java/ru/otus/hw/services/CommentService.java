package ru.otus.hw.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Comment;

public interface CommentService {

    Mono<Comment> findById(Long id);

    Flux<Comment> findAllByBookId(Long bookId);

    Mono<Comment> create(Long bookId, String text);

    Mono<Comment> update(Long id, String newText);

    Mono<Void> deleteById(Long id);
}
