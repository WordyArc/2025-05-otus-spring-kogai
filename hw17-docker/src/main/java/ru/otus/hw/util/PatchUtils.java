package ru.otus.hw.util;

import lombok.experimental.UtilityClass;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class PatchUtils {

    public record BookPatch(String title, Long authorId, Set<Long> genreIds) {
    }

    public BookPatch mergeBookPatch(Map<String, Object> patch, Book existing) {
        var title = (String) patch.getOrDefault("title", existing.getTitle());
        if (patch.containsKey("title") && (title == null || title.isBlank())) {
            throw new IllegalArgumentException("Title must not be blank");
        }

        Long authorId = patch.containsKey("authorId")
                ? toLong(patch.get("authorId"))
                : existing.getAuthor().getId();

        Set<Long> genreIds = patch.containsKey("genreIds")
                ? ((List<?>) patch.get("genreIds")).stream()
                .map(PatchUtils::toLong)
                .collect(Collectors.toSet())
                : existing.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        return new BookPatch(title, authorId, genreIds);
    }

    private Long toLong(Object value) {
        return ((Number) value).longValue();
    }

}
