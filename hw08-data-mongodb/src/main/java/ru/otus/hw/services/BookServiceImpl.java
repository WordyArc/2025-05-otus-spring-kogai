package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
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

    private final CommentRepository commentRepository;

    @Override
    public Optional<Book> findById(String id) {
        return bookRepository.findById(id);
    }

    @Override
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Override
    public Book insert(String title, String authorId, Set<String> genresIds) {
        return save(null, title, authorId, genresIds);
    }

    @Override
    public Book update(String id, String title, String authorId, Set<String> genresIds) {
        if (!bookRepository.existsById(id)) {
            throw new EntityNotFoundException("Book with id %s not found".formatted(id));
        }
        return save(id, title, authorId, genresIds);
    }

    @Override
    public void deleteById(String id) {
        commentRepository.deleteByBookId(id);
        bookRepository.deleteById(id);
    }

    private Book save(String id, String title, String authorId, Set<String> genresIds) {
        if (genresIds == null || genresIds.isEmpty()) {
            throw new IllegalArgumentException("Genres ids must not be null or empty");
        }

        var author = authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Author with id %s not found".formatted(authorId)));
        var genres = genreRepository.findAllByIdIn(genresIds);
        if (genres.size() != genresIds.size()) {
            throw new EntityNotFoundException("One or all genres with ids %s not found".formatted(genresIds));
        }

        var book = new Book(id, title, author, genres);
        return bookRepository.save(book);
    }
}
