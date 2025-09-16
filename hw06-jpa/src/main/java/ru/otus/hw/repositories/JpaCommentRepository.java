package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Comment;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaCommentRepository implements CommentRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Comment> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Comment.class, id));
    }

    @Override
    public List<Comment> findAllByBookId(Long bookId) {
        return entityManager.createQuery("""
                        select c from Comment c
                        where c.book.id = :bookId
                        order by c.id
                        """, Comment.class)
                .setParameter("bookId", bookId)
                .getResultList();
    }

    @Override
    public Comment save(Comment comment) {
        if (comment.getId() == null) {
            entityManager.persist(comment);
            return comment;
        }
        return entityManager.merge(comment);
    }

    @Override
    public void deleteById(Long id) {
        var comment = entityManager.find(Comment.class, id);
        if (comment != null) entityManager.remove(comment);
    }
}
