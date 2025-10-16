package ru.otus.hw.integration;


import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenLibraryService {

    private final OpenLibraryClient openLibraryClient;

    @Retry(name = "openlibrary")
    @Bulkhead(name = "openlibrary")
    @RateLimiter(name = "openlibrary")
    @CircuitBreaker(name = "openlibrary", fallbackMethod = "searchByTitleFallback")
    public List<BookDto> searchByTitle(String title, int limit) {
        var response = openLibraryClient.searchByTitle(title, limit);
        var docs = response == null || response.docs() == null
                ? List.<OpenLibrarySearchResponse.Doc>of()
                : response.docs();
        return docs.stream()
                .map(d -> new BookDto(
                        null,
                        d.title(),
                        new AuthorDto(null, d.authorName() == null || d.authorName().isEmpty()
                                ? "-"
                                : d.authorName().get(0)),
                        List.of(new GenreDto(null, "External"))
                ))
                .toList();
    }

    private List<BookDto> searchByTitleFallback(String title, int limit, Throwable t) {
        return List.of();
    }
}
