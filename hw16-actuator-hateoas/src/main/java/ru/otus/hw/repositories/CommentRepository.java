package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.otus.hw.models.Comment;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(path = "comments", collectionResourceRel = "comments", itemResourceRel = "comment")
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Override
    @EntityGraph(attributePaths = "book")
    Optional<Comment> findById(Long id);

    @EntityGraph(attributePaths = "book")
    List<Comment> findAllByBookId(Long bookId);
}
