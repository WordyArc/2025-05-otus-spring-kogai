package ru.otus.hw.command;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.Properties;

@ShellComponent
@RequiredArgsConstructor
public class MigrationCommands {

    private final JobOperator jobOperator;

    private final JobExplorer jobExplorer;

    @ShellMethod(key = "migrate-r2m", value = "Run migration RDBMS -> Mongo")
    public String start() throws Exception {
        var props = new Properties();
        props.setProperty("startedAt", Long.toString(System.currentTimeMillis()));
        long execId = jobOperator.start("rdbmsToMongoJob", props);
        return "Started executionId=" + execId;
    }

    @ShellMethod(key = "restart-last", value = "Restart last failed execution")
    public String restart() throws Exception {
        var last = jobExplorer.getJobInstances("rdbmsToMongoJob", 0, 1)
                .stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No job instances"));

        var failed = jobExplorer.getJobExecutions(last).stream()
                .filter(e -> e.getStatus().isUnsuccessful())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No failed executions"));

        long newId = jobOperator.restart(failed.getId());
        return "Restarted executionId=" + newId;
    }
}
