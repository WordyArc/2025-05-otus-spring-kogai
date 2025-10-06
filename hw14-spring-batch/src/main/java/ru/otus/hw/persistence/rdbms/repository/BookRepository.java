package ru.otus.hw.persistence.rdbms.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.persistence.rdbms.model.Book;

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
