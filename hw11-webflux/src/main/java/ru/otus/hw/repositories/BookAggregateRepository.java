package ru.otus.hw.repositories;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Book;

public interface BookAggregateRepository {

    Flux<Book> findAllAggregates();

    Mono<Book> findAggregateById(Long id);

    Mono<Void> replaceGenres(Long bookId, Iterable<Long> genreIds);
}
