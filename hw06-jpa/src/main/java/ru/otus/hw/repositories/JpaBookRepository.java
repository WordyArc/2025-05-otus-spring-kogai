package ru.otus.hw.repositories;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class JpaBookRepository implements BookRepository {

    private static final String FETCH_GRAPH_HINT = "jakarta.persistence.fetchgraph";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Book> findById(Long id) {
        EntityGraph<?> graph = entityManager.getEntityGraph("book.withAuthorAndGenres");
        Map<String, Object> hints = Map.of(FETCH_GRAPH_HINT, graph);
        return Optional.ofNullable(entityManager.find(Book.class, id, hints));
    }

    @Override
    public List<Book> findAll() {
        EntityGraph<?> graph = entityManager.getEntityGraph("book.withAuthorAndGenres");
        return entityManager.createQuery("select distinct b from Book b order by b.id", Book.class)
                .setHint(FETCH_GRAPH_HINT, graph)
                .getResultList();
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == null) {
            entityManager.persist(book);
            return book;
        }

        Book managed = entityManager.find(Book.class, book.getId());
        if (managed == null) {
            throw new EntityNotFoundException("Book with id %d not found".formatted(book.getId()));
        }
        managed.setTitle(book.getTitle());
        managed.setAuthor(book.getAuthor());

        Set<Genre> newGenres = (book.getGenres() == null)
                ? Collections.emptySet()
                : new HashSet<>(book.getGenres());
        managed.getGenres().clear();
        managed.getGenres().addAll(newGenres);
        return managed;
    }

    @Override
    public void deleteById(Long id) {
        Book targetBook = entityManager.find(Book.class, id);
        if (targetBook != null) {
            entityManager.remove(targetBook);
        }
    }
}
