package ru.otus.hw.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Book;


@Repository
public interface BookRepository extends ReactiveCrudRepository<Book, Long>, BookAggregateRepository {
}
