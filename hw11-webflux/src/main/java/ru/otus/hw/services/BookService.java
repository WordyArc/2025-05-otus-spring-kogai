package ru.otus.hw.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Book;

import java.util.Set;

public interface BookService {
    Mono<Book> getById(Long id);

    Flux<Book> findAll();

    Mono<Book> insert(String title, Long authorId, Set<Long> genresIds);

    Mono<Book> update(Long id, String title, Long authorId, Set<Long> genresIds);

    Mono<Void> deleteById(Long id);
}
