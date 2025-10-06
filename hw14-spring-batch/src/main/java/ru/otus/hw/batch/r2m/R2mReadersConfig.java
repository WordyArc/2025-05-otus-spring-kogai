package ru.otus.hw.batch.r2m;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.otus.hw.persistence.rdbms.model.Author;
import ru.otus.hw.persistence.rdbms.model.Book;
import ru.otus.hw.persistence.rdbms.model.Comment;
import ru.otus.hw.persistence.rdbms.model.Genre;

@Configuration
@RequiredArgsConstructor
public class R2mReadersConfig {

    private final EntityManagerFactory emf;

    @Bean
    @StepScope
    public JpaPagingItemReader<Author> authorReader() {
        return new JpaPagingItemReaderBuilder<Author>()
                .name("authorReader")
                .entityManagerFactory(emf)
                .queryString("select a from Author a order by a.id")
                .pageSize(500)
                .saveState(true)
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<Genre> genreReader() {
        return new JpaPagingItemReaderBuilder<Genre>()
                .name("genreReader")
                .entityManagerFactory(emf)
                .queryString("select g from Genre g order by g.id")
                .pageSize(1000)
                .saveState(true)
                .build();
    }

    @Bean
    @StepScope
    public JpaCursorItemReader<Book> bookReader() {
        return new JpaCursorItemReaderBuilder<Book>()
                .name("bookReader")
                .entityManagerFactory(emf)
                .queryString("""
                        select b
                        from Book b
                        join fetch b.author a
                        order by b.id
                        """)
                .saveState(true)
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<Comment> commentReader() {
        return new JpaPagingItemReaderBuilder<Comment>()
                .name("commentReader")
                .entityManagerFactory(emf)
                .queryString("select c from Comment c order by c.id")
                .pageSize(1000)
                .saveState(true)
                .build();
    }

}
