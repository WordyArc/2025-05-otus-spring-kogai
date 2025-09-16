package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class JpaBookRepository implements BookRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Book> findById(Long id) {
        List<Book> books = entityManager.createQuery("""
                        SELECT DISTINCT b
                        FROM Book b
                          JOIN FETCH b.author a
                          LEFT JOIN FETCH b.genres g
                        WHERE b.id = :id
                        """, Book.class)
                .setParameter("id", id)
                .getResultList();
        return books.stream().findFirst();
    }

    @Override
    public List<Book> findAll() {
        List<Book> books = entityManager.createQuery("""
                SELECT b FROM Book b
                    JOIN FETCH b.author a
                ORDER BY b.id
                """, Book.class).getResultList();

        books.forEach(book -> Hibernate.initialize(book.getGenres()));
        return books;
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
