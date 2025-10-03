package ru.otus.hw.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.test.annotation.DirtiesContext;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;
import ru.otus.hw.services.BookService;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MethodAclBookServiceTest extends AbstractMethodAclTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private AuthorRepository authors;

    @Autowired
    private GenreRepository genres;

    @Autowired
    private BookRepository books;

    @Test
    @DisplayName("findAll returns entries with READ permission for the user")
    void findAllRequiresRead() {
        var author = authors.save(new Author(null, "Author"));
        var genre = genres.save(new Genre(null, "Genre"));

        authenticate("user1");
        var firstBook = bookService.insert("First", author.getId(), Set.of(genre.getId()));

        authenticate("user2");
        var secondBook = bookService.insert("Second", author.getId(), Set.of(genre.getId()));

        authenticate("user1");
        var user1Books = bookService.findAll();
        assertThat(user1Books).extracting(Book::getId).containsExactly(firstBook.getId());

        authenticate("user2");
        var user2Books = bookService.findAll();
        assertThat(user2Books).extracting(Book::getId).containsExactly(secondBook.getId());
    }

    @Test
    @DisplayName("getById requires READ permission")
    void getByIdRequiresRead() {
        var author = authors.save(new Author(null, "Author"));
        var genre = genres.save(new Genre(null, "Genre"));

        authenticate("owner");
        var securedBook = bookService.insert("Hidden", author.getId(), Set.of(genre.getId()));

        authenticate("guest");
        assertThatThrownBy(() -> bookService.getById(securedBook.getId()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("update requires WRITE permission")
    void updateRequiresWrite() {
        var author = authors.save(new Author(null, "Author"));
        var genre = genres.save(new Genre(null, "Genre"));

        authenticate("user1");
        var editable = bookService.insert("Draft", author.getId(), Set.of(genre.getId()));

        var updated = bookService.update(editable.getId(), "Reviewed", author.getId(), Set.of(genre.getId()));
        assertThat(updated.getTitle()).isEqualTo("Reviewed");

        authenticate("user2");
        assertThatThrownBy(() -> bookService.update(editable.getId(), "Rejected", author.getId(), Set.of(genre.getId())))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("delete requires DELETE permission or ADMIN role")
    void deleteRequiresDeleteOrAdmin() {
        var author = authors.save(new Author(null, "Author"));
        var genre = genres.save(new Genre(null, "Genre"));

        authenticate("user1");
        var firstBook = bookService.insert("Removable", author.getId(), Set.of(genre.getId()));

        grantPermission(Book.class, firstBook.getId(), "user2", BasePermission.DELETE);

        authenticate("user2");
        bookService.deleteById(firstBook.getId());
        assertThat(books.findById(firstBook.getId())).isEmpty();

        authenticate("user1");
        var secondBook = bookService.insert("RemovableAgain", author.getId(), Set.of(genre.getId()));

        authenticate("admin", "ADMIN");
        bookService.deleteById(secondBook.getId());
        assertThat(books.findById(secondBook.getId())).isEmpty();
    }
}
