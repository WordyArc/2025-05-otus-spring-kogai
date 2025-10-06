package ru.otus.hw.migration.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.otus.hw.jpa.models.Author;
import ru.otus.hw.jpa.models.Book;
import ru.otus.hw.jpa.models.Comment;
import ru.otus.hw.jpa.models.Genre;
import ru.otus.hw.migration.mappers.AuthorMapper;
import ru.otus.hw.migration.mappers.BookMapper;
import ru.otus.hw.migration.mappers.CommentMapper;
import ru.otus.hw.migration.mappers.GenreMapper;
import ru.otus.hw.mongo.models.AuthorDocument;
import ru.otus.hw.mongo.models.BookDocument;
import ru.otus.hw.mongo.models.CommentDocument;
import ru.otus.hw.mongo.models.GenreDocument;

@Configuration
@RequiredArgsConstructor
class ProcessorsConfig {

    private final AuthorMapper am;

    private final GenreMapper gm;

    private final BookMapper bm;

    private final CommentMapper cm;

    @Bean
    @StepScope
    ItemProcessor<Author, AuthorDocument> authorProcessor() {
        return am::toDocument;
    }

    @Bean
    @StepScope
    ItemProcessor<Genre, GenreDocument> genreProcessor() {
        return gm::toDocument;
    }

    @Bean
    @StepScope
    ItemProcessor<Book, BookDocument> bookProcessor() {
        return bm::toDocument;
    }

    @Bean
    @StepScope
    ItemProcessor<Comment, CommentDocument> commentProcessor() {
        return cm::toDocument;
    }
}
