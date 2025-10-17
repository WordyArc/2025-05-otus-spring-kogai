package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private final AclBookService aclBookService;

    @Override
    @Transactional(readOnly = true)
    @PostFilter("hasPermission(filterObject, 'READ')")
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasPermission(#id, 'ru.otus.hw.models.Book', 'READ')")
    public Book getById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    @PostAuthorize("returnObject.isEmpty() or hasPermission(returnObject.get(), 'READ')")
    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public Book insert(String title, Long authorId, Set<Long> genresIds) {
        var saved = save(null, title, authorId, genresIds);
        aclBookService.createDefaultAcl(saved);
        return saved;
    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(#id, 'ru.otus.hw.models.Book', 'WRITE')")
    public Book update(Long id, String title, Long authorId, Set<Long> genresIds) {
        if (!bookRepository.existsById(id)) {
            throw new EntityNotFoundException("Book with id %d not found".formatted(id));
        }
        return save(id, title, authorId, genresIds);
    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(#id, 'ru.otus.hw.models.Book', 'DELETE') or hasRole('ADMIN')")
    public void deleteById(Long id) {
        aclBookService.deleteAcl(id);
        bookRepository.deleteById(id);
    }

    private Book save(Long id, String title, Long authorId, Set<Long> genresIds) {
        if (genresIds == null || genresIds.isEmpty()) {
            throw new IllegalArgumentException("Genres ids must not be null or empty");
        }

        var author = authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Author with id %d not found".formatted(authorId)));
        var genres = genreRepository.findAllByIdIn(genresIds);
        if (genres.size() != genresIds.size()) {
            throw new EntityNotFoundException("One or all genres with ids %s not found".formatted(genresIds));
        }

        var book = new Book(id, title, author, genres);
        return bookRepository.save(book);
    }
}
