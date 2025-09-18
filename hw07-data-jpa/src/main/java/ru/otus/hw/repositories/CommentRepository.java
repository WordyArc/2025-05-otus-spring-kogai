package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.otus.hw.models.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Override
    @EntityGraph(attributePaths = "book")
    Optional<Comment> findById(Long id);

    @Query("""
        select c
        from Comment c
        join fetch c.book b
        where b.id = :bookId
        """)
    List<Comment> findAllByBookId(@Param("bookId") Long bookId);
}
