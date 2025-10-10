package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(path = "books", collectionResourceRel = "books", itemResourceRel = "book")
public interface BookRepository extends JpaRepository<Book, Long> {

    @Override
    @EntityGraph(value = "book.withAuthorAndGenres")
    Optional<Book> findById(Long id);

    @Override
    @EntityGraph(value = "book.withAuthorAndGenres")
    List<Book> findAll();
}
