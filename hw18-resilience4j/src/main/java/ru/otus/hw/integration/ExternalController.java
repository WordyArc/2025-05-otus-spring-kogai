package ru.otus.hw.integration;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.BookDto;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/external")
public class ExternalController {

    private final OpenLibraryService bookSearchService;

    private final HttpBinService httpBinService;

    @GetMapping("/books/search")
    public List<BookDto> search(@RequestParam String title,
                                @RequestParam(defaultValue = "5") int limit) {
        return bookSearchService.searchByTitle(title, limit);
    }

    @GetMapping("/books/search-retry-test")
    public List<BookDto> searchRetryTest(@RequestParam String title,
                                         @RequestParam(defaultValue = "2") int limit,
                                         @RequestParam(defaultValue = "false") boolean fail) throws IOException {
        return bookSearchService.searchByTitleWithRetryTest(title, limit, fail);
    }

    @GetMapping("/httpbin/delay")
    public CompletableFuture<Map<String, Object>> delay(@RequestParam(defaultValue = "5") int seconds) {
        return httpBinService.delayAsync(seconds);
    }
}
