package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Author;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaAuthorRepository implements AuthorRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Author> findAll() {
        return entityManager.createQuery("SELECT a from Author a order by a.id", Author.class)
        .getResultList();
    }

    @Override
    public Optional<Author> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Author.class, id));
    }
}
