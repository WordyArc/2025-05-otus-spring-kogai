package ru.otus.hw.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "openlibrary",
        url = "${integrations.openlibrary.url:https://openlibrary.org}"
)
public interface OpenLibraryClient {

    @GetMapping(path = "/search.json")
    OpenLibrarySearchResponse searchByTitle(
            @RequestParam("title") String title,
            @RequestParam(value = "limit", defaultValue = "5") int limit
    );

    @GetMapping(path = "/search.json")
    OpenLibrarySearchResponse search(@RequestParam("q") String query);


}
