package ru.otus.hw.jpa.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.jpa.models.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Override
    @EntityGraph(attributePaths = "book")
    Optional<Comment> findById(Long id);

    @EntityGraph(attributePaths = "book")
    List<Comment> findAllByBookId(Long bookId);
}
