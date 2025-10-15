package ru.otus.hw.integration;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenLibrarySearchResponse(
        @JsonProperty("numFound") int numFound,
        @JsonProperty("docs") List<Doc> docs
) {
    public record Doc(
            String title,
            @JsonProperty("author_name") List<String> authorName,
            String key
    ) { }
}
