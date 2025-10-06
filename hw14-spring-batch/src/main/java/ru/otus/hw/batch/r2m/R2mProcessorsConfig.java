package ru.otus.hw.batch.r2m;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.otus.hw.persistence.rdbms.model.Author;
import ru.otus.hw.persistence.rdbms.model.Book;
import ru.otus.hw.persistence.rdbms.model.Comment;
import ru.otus.hw.persistence.rdbms.model.Genre;
import ru.otus.hw.batch.r2m.mapper.AuthorMapper;
import ru.otus.hw.batch.r2m.mapper.BookMapper;
import ru.otus.hw.batch.r2m.mapper.CommentMapper;
import ru.otus.hw.batch.r2m.mapper.GenreMapper;
import ru.otus.hw.persistence.mongo.model.AuthorDocument;
import ru.otus.hw.persistence.mongo.model.BookDocument;
import ru.otus.hw.persistence.mongo.model.CommentDocument;
import ru.otus.hw.persistence.mongo.model.GenreDocument;

@Configuration
@RequiredArgsConstructor
class R2mProcessorsConfig {

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
