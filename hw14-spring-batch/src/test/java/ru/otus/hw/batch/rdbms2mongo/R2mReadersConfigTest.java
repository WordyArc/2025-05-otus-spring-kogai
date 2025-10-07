package ru.otus.hw.batch.rdbms2mongo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.otus.hw.persistence.rdbms.model.Author;
import ru.otus.hw.persistence.rdbms.model.Book;
import ru.otus.hw.persistence.rdbms.model.Comment;
import ru.otus.hw.persistence.rdbms.model.Genre;
import ru.otus.hw.persistence.rdbms.repository.AuthorRepository;
import ru.otus.hw.persistence.rdbms.repository.BookRepository;
import ru.otus.hw.persistence.rdbms.repository.CommentRepository;
import ru.otus.hw.persistence.rdbms.repository.GenreRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SpringBatchTest
class R2mReadersConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private AuthorRepository rAuthors;
    @Autowired
    private GenreRepository rGenres;
    @Autowired
    private BookRepository rBooks;
    @Autowired
    private CommentRepository rComments;

    @Test
    @DisplayName("Comments step runs worker partitions")
    void commentsPartitionWorkersExecuted() throws Exception {
        var a = rAuthors.save(new Author(null, "A"));
        var g = rGenres.save(new Genre(null, "G"));
        var b = rBooks.save(new Book(null, "B", a, new ArrayList<>(List.of(g))));
        rComments.save(new Comment(null, "c1", b, LocalDateTime.now()));
        rComments.save(new Comment(null, "c2", b, LocalDateTime.now()));

        JobExecution exec = jobLauncherTestUtils.launchJob(new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters());
        assertThat(exec.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        long workerSteps = exec.getStepExecutions().stream()
                .map(StepExecution::getStepName)
                .filter(name -> name.startsWith("commentsWorker"))
                .count();

        assertThat(workerSteps).isGreaterThanOrEqualTo(1);
    }

}