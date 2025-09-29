package ru.otus.hw.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.otus.hw.models.Genre;

@Repository
public interface GenreRepository extends ReactiveCrudRepository<Genre, Long> {

    @Override
    Flux<Genre> findAllById(Iterable<Long> ids);
}
