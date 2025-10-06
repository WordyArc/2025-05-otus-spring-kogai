package ru.otus.hw.batch.r2m;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import ru.otus.hw.persistence.rdbms.model.Author;
import ru.otus.hw.persistence.rdbms.model.Book;
import ru.otus.hw.persistence.rdbms.model.Comment;
import ru.otus.hw.persistence.rdbms.model.Genre;
import ru.otus.hw.batch.r2m.listener.JobLoggingListener;
import ru.otus.hw.batch.r2m.listener.StepLoggingListener;
import ru.otus.hw.batch.r2m.listener.ThrottledChunkProgressListener;
import ru.otus.hw.config.BatchProperties;
import ru.otus.hw.persistence.mongo.model.AuthorDocument;
import ru.otus.hw.persistence.mongo.model.BookDocument;
import ru.otus.hw.persistence.mongo.model.CommentDocument;
import ru.otus.hw.persistence.mongo.model.GenreDocument;

@Configuration
@RequiredArgsConstructor
public class R2mJobConfig {

    private final JobRepository jobRepository;

    private final PlatformTransactionManager transactionManager;

    private final BatchProperties batchProperties;


    private final ItemReader<Author> authorReader;

    private final ItemProcessor<Author, AuthorDocument> authorProcessor;

    private final ItemWriter<AuthorDocument> authorWriter;


    private final ItemReader<Genre> genreReader;

    private final ItemProcessor<Genre, GenreDocument> genreProcessor;

    private final ItemWriter<GenreDocument> genreWriter;


    private final ItemReader<Book> bookReader;

    private final ItemProcessor<Book, BookDocument> bookProcessor;

    private final ItemWriter<BookDocument> bookWriter;


    private final ItemReader<Comment> commentReader;

    private final ItemProcessor<Comment, CommentDocument> commentProcessor;

    private final ItemWriter<CommentDocument> commentWriter;

    @Bean
    JobLoggingListener jobLoggingListener() {
        return new JobLoggingListener();
    }

    @Bean
    StepLoggingListener authorsStepListener() {
        return new StepLoggingListener("authors");
    }

    @Bean
    StepLoggingListener genresStepListener() {
        return new StepLoggingListener("genres");
    }

    @Bean
    StepLoggingListener booksStepListener() {
        return new StepLoggingListener("books");
    }

    @Bean
    StepLoggingListener commentsStepListener() {
        return new StepLoggingListener("comments");
    }

    @Bean
    ThrottledChunkProgressListener authorsProgress() {
        return new ThrottledChunkProgressListener("authors", 
                batchProperties.getProgressListeners().getAuthors());
    }

    @Bean
    ThrottledChunkProgressListener genresProgress() {
        return new ThrottledChunkProgressListener("genres", 
                batchProperties.getProgressListeners().getGenres());
    }

    @Bean
    ThrottledChunkProgressListener booksProgress() {
        return new ThrottledChunkProgressListener("books", 
                batchProperties.getProgressListeners().getBooks());
    }

    @Bean
    ThrottledChunkProgressListener commentsProgress() {
        return new ThrottledChunkProgressListener("comments", 
                batchProperties.getProgressListeners().getComments());
    }

    @Bean
    public TaskExecutor splitExecutor() {
        var threadPool = new ThreadPoolTaskExecutor();
        var config = batchProperties.getThreadPool();
        threadPool.setThreadNamePrefix(config.getThreadNamePrefix());
        threadPool.setCorePoolSize(config.getCorePoolSize());
        threadPool.setMaxPoolSize(config.getMaxPoolSize());
        threadPool.setQueueCapacity(config.getQueueCapacity());
        threadPool.initialize();
        return threadPool;
    }

    @Bean
    public Step authorsStep() {
        return new StepBuilder("authorsStep", jobRepository)
                .<Author, AuthorDocument>chunk(batchProperties.getChunkSizes().getAuthors(), transactionManager)
                .reader(authorReader)
                .processor(authorProcessor)
                .writer(authorWriter)
                .listener(authorsStepListener())
                .listener(authorsProgress())
                .faultTolerant()
                .retry(DataAccessResourceFailureException.class)
                .retryLimit(3)
                .build();
    }

    @Bean
    public Step genresStep() {
        return new StepBuilder("genresStep", jobRepository)
                .<Genre, GenreDocument>chunk(batchProperties.getChunkSizes().getGenres(), transactionManager)
                .reader(genreReader)
                .processor(genreProcessor)
                .writer(genreWriter)
                .listener(genresStepListener())
                .listener(genresProgress())
                .build();
    }

    @Bean
    public Step booksStep() {
        return new StepBuilder("booksStep", jobRepository)
                .<Book, BookDocument>chunk(batchProperties.getChunkSizes().getBooks(), transactionManager)
                .reader(bookReader)
                .processor(bookProcessor)
                .writer(bookWriter)
                .listener(booksStepListener())
                .listener(booksProgress())
                .build();
    }

    @Bean
    public Step commentsStep() {
        return new StepBuilder("commentsStep", jobRepository)
                .<Comment, CommentDocument>chunk(batchProperties.getChunkSizes().getComments(), transactionManager)
                .reader(commentReader)
                .processor(commentProcessor)
                .writer(commentWriter)
                .listener(commentsStepListener())
                .listener(commentsProgress())
                .build();
    }

    @Bean
    public Job rdbmsToMongoJob() {
        return new JobBuilder("rdbmsToMongoJob", jobRepository)
                .listener(jobLoggingListener())
                .start(parallelAuthorsAndGenres(splitExecutor()))
//                .next(booksStep())
//                .next(commentsStep())
                .next(parallelBooksAndComments(splitExecutor()))
                .end()
                .build();
    }

    private Flow authorsFlow() {
        return new FlowBuilder<SimpleFlow>("authorsFlow")
                .start(authorsStep())
                .build();
    }

    private Flow genresFlow() {
        return new FlowBuilder<SimpleFlow>("genresFlow")
                .start(genresStep())
                .build();
    }

    private Flow parallelAuthorsAndGenres(TaskExecutor taskExecutor) {
        return new FlowBuilder<SimpleFlow>("r2mFlow")
                .split(taskExecutor)
                .add(authorsFlow(), genresFlow())
                .build();
    }

    private Flow booksFlow() {
        return new FlowBuilder<SimpleFlow>("booksFlow").start(booksStep()).build();
    }

    private Flow commentsFlow() {
        return new FlowBuilder<SimpleFlow>("commentsFlow").start(commentsStep()).build();
    }

    private Flow parallelBooksAndComments(TaskExecutor exec) {
        return new FlowBuilder<SimpleFlow>("r2mFlow2")
                .split(exec)
                .add(booksFlow(), commentsFlow())
                .build();
    }

}

