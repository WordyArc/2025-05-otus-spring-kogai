package ru.otus.hw.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class MethodAclCommentServiceTest extends AbstractMethodAclTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    @DisplayName("findAllByBookId requires READ permission")
    void findAllRequiresRead() {
        var ids = prepareBook("user1");

        authenticate("user2");
        assertThatThrownBy(() -> commentService.findAllByBookId(ids.bookId))
                .isInstanceOf(AccessDeniedException.class);

        grantPermission(Book.class, ids.bookId, "user2", BasePermission.READ);
        var result = commentService.findAllByBookId(ids.bookId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("create requires WRITE permission")
    void createRequiresWrite() {
        var ids = prepareBook("user1");

        authenticate("user1");
        var comment = commentService.create(ids.bookId, "text");
        assertThat(comment.getId()).isNotNull();

        authenticate("user2");
        assertThatThrownBy(() -> commentService.create(ids.bookId, "text"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("update requires WRITE permission")
    void updateRequiresWrite() {
        var ids = prepareBook("user1");
        authenticate("user1");
        var comment = commentService.create(ids.bookId, "original");

        authenticate("user2");
        assertThatThrownBy(() -> commentService.update(comment.getId(), "modified"))
                .isInstanceOf(AccessDeniedException.class);

        grantPermission(Book.class, ids.bookId, "user2", BasePermission.WRITE);
        var updated = commentService.update(comment.getId(), "modified");

        assertThat(updated.getText()).isEqualTo("modified");
    }

    @Test
    @DisplayName("deleteById requires DELETE permission")
    void deleteRequiresDelete() {
        var ids = prepareBook("user1");
        authenticate("user1");
        var comment = commentService.create(ids.bookId, "text");

        authenticate("user2");
        assertThatThrownBy(() -> commentService.deleteById(comment.getId()))
                .isInstanceOf(AccessDeniedException.class);

        grantPermission(Book.class, ids.bookId, "user2", BasePermission.DELETE);
        commentService.deleteById(comment.getId());

        assertThat(commentRepository.findById(comment.getId())).isEmpty();
    }

    private record BookIds(Long authorId, Long genreId, Long bookId) {
    }

    private BookIds prepareBook(String owner) {
        var author = authorRepository.save(new Author(null, "Author"));
        var genre = genreRepository.save(new Genre(null, "Genre"));
        authenticate(owner);
        var book = bookService.insert("Book", author.getId(), Set.of(genre.getId()));
        return new BookIds(author.getId(), genre.getId(), book.getId());
    }
}