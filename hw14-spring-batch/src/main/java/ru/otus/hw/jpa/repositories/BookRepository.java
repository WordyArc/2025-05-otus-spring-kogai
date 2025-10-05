package ru.otus.hw.jpa.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.jpa.models.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Override
    @EntityGraph(value = "book.withAuthorAndGenres")
    Optional<Book> findById(Long id);

    @Override
    @EntityGraph(value = "book.withAuthorAndGenres")
    List<Book> findAll();
}
