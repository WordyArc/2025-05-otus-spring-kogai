package ru.otus.hw.batch.rdbms2mongo;

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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import ru.otus.hw.batch.rdbms2mongo.listener.CommentPartitionInfoListener;
import ru.otus.hw.persistence.rdbms.model.Author;
import ru.otus.hw.persistence.rdbms.model.Book;
import ru.otus.hw.persistence.rdbms.model.Comment;
import ru.otus.hw.persistence.rdbms.model.Genre;
import ru.otus.hw.batch.rdbms2mongo.listener.JobLoggingListener;
import ru.otus.hw.batch.rdbms2mongo.listener.StepLoggingListener;
import ru.otus.hw.batch.rdbms2mongo.listener.ThrottledChunkProgressListener;
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

    private final BatchProperties props;


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

    private final ItemReader<Comment> partitionedCommentReader;

    @Bean
    public TaskExecutor splitExecutor() {
        var threadPool = new ThreadPoolTaskExecutor();
        var config = props.getThreadPool();
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
                .<Author, AuthorDocument>chunk(props.getChunkSizes().getAuthors(), transactionManager)
                .reader(authorReader)
                .processor(authorProcessor)
                .writer(authorWriter)
                .listener(new StepLoggingListener("authors"))
                .listener(new ThrottledChunkProgressListener("authors",
                        props.getProgressListeners().getAuthors())
                )
                .build();
    }

    @Bean
    public Step genresStep() {
        return new StepBuilder("genresStep", jobRepository)
                .<Genre, GenreDocument>chunk(props.getChunkSizes().getGenres(), transactionManager)
                .reader(genreReader)
                .processor(genreProcessor)
                .writer(genreWriter)
                .listener(new StepLoggingListener("genres"))
                .listener(new ThrottledChunkProgressListener("genres",
                        props.getProgressListeners().getGenres())
                )
                .build();
    }

    @Bean
    public Step booksStep() {
        return new StepBuilder("booksStep", jobRepository)
                .<Book, BookDocument>chunk(props.getChunkSizes().getBooks(), transactionManager)
                .reader(bookReader)
                .processor(bookProcessor)
                .writer(bookWriter)
                .listener(new StepLoggingListener("books"))
                .listener(new ThrottledChunkProgressListener("books",
                        props.getProgressListeners().getBooks())
                )
                .build();
    }

    @Bean
    public Step commentsWorkerStep() {
        return new StepBuilder("commentsWorker", jobRepository)
                .<Comment, CommentDocument>chunk(props.getChunkSizes().getComments(), transactionManager)
                .reader(partitionedCommentReader)
                .processor(commentProcessor)
                .writer(commentWriter)
                .listener(new CommentPartitionInfoListener())
                .listener(new StepLoggingListener("comments"))
                .listener(new ThrottledChunkProgressListener("comments",
                        props.getProgressListeners().getComments())
                )
                .build();
    }

    @Bean
    public Step commentsPartitionedStep() {
        int partitions = Math.max(1, props.getThreadPool().getCorePoolSize());
        return new StepBuilder("commentsPartitioned", jobRepository)
                .partitioner("commentsWorker", new RoundRobinPartitioner(partitions))
                .step(commentsWorkerStep())
                .gridSize(partitions)
                .taskExecutor(splitExecutor())
                .build();
    }


    @Bean
    public Job rdbmsToMongoJob() {
        return new JobBuilder("rdbmsToMongoJob", jobRepository)
                .listener(new JobLoggingListener())
                .start(parallelAuthorsAndGenres(splitExecutor()))
                .next(parallelBooksAndComments(splitExecutor()))
                .end()
                .build();
    }

    private Flow parallelAuthorsAndGenres(TaskExecutor taskExecutor) {
        return new FlowBuilder<SimpleFlow>("r2mFlow")
                .split(taskExecutor)
                .add(flowOf(authorsStep(), genresStep()))
                .build();
    }

    private Flow parallelBooksAndComments(TaskExecutor exec) {
        return new FlowBuilder<SimpleFlow>("r2mFlow2")
                .split(exec)
                .add(flowOf(booksStep()), flowOf(commentsPartitionedStep()))
                .build();
    }

    private Flow flowOf(Step... steps) {
        if (steps == null || steps.length == 0) {
            throw new IllegalArgumentException("At least one step is required to build a flow");
        }

        var flowBuilder = new FlowBuilder<SimpleFlow>(steps[0].getName() + "Flow")
                .start(steps[0]);
        for (int i = 1; i < steps.length; i++) {
            flowBuilder.next(steps[i]);
        }
        return flowBuilder.build();
    }
}
