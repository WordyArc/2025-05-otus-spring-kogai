package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public Mono<Book> getById(Long id) {
        return bookRepository.findAggregateById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Book with id %d not found".formatted(id))));
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<Book> findAll() {
        return bookRepository.findAllAggregates();
    }

    @Override
    @Transactional
    public Mono<Book> insert(String title, Long authorId, Set<Long> genresIds) {
        return validateForUpsert(authorId, genresIds)
                .then(saveAndReload(null, title, authorId, genresIds));
    }

    @Override
    @Transactional
    public Mono<Book> update(Long id, String title, Long authorId, Set<Long> genresIds) {
        return Mono.when(
                ensureBookExists(id),
                validateForUpsert(authorId, genresIds)
        ).then(saveAndReload(id, title, authorId, genresIds));
    }

    @Override
    @Transactional
    public Mono<Void> deleteById(Long id) {
        return bookRepository.deleteById(id);
    }

    private Mono<Void> validateForUpsert(Long authorId, Set<Long> genresIds) {
        if (genresIds == null || genresIds.isEmpty()) {
            return Mono.error(new IllegalArgumentException("Genres ids must not be null or empty"));
        }

        Mono<Void> ensureAuthor = authorRepository.existsById(authorId)
                .flatMap(exists -> exists
                        ? Mono.empty()
                        : Mono.error(new EntityNotFoundException("Author with id %d not found".formatted(authorId))));

        Mono<Void> ensureGenres = genreRepository.findAllById(genresIds)
                .count()
                .flatMap(cnt -> cnt == genresIds.size()
                        ? Mono.empty()
                        : Mono.error(new EntityNotFoundException("One or all genres with ids %s not found".formatted(genresIds))));

        return Mono.when(ensureAuthor, ensureGenres);
    }

    private Mono<Void> ensureBookExists(Long id) {
        return bookRepository.existsById(id)
                .flatMap(exists -> exists
                        ? Mono.empty()
                        : Mono.error(new EntityNotFoundException("Book with id %d not found".formatted(id))));
    }

    private Mono<Book> saveAndReload(Long id, String title, Long authorId, Set<Long> genresIds) {
        return bookRepository.save(new Book(id, title, authorId, null, null))
                .flatMap(saved -> bookRepository.replaceGenres(saved.getId(), genresIds).thenReturn(saved))
                .flatMap(saved -> getById(saved.getId()));
    }
}
