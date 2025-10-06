package ru.otus.hw.command;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.util.MongoMigrationUtils;

import java.util.List;
import java.util.Properties;

@ShellComponent
@RequiredArgsConstructor
public class MigrationCommands {

    private final JobOperator jobOperator;

    private final JobExplorer jobExplorer;

    private final MongoTemplate mongo;

    @ShellMethod(key = {"migrate-r2m", "r2m"}, value = "Run migration RDBMS -> Mongo")
    public String start() throws Exception {
        var props = new Properties();
        props.setProperty("startedAt", Long.toString(System.currentTimeMillis()));
        long execId = jobOperator.start("rdbmsToMongoJob", props);
        return "Started executionId=" + execId;
    }

    @ShellMethod(key = {"restart", "rs", "r"}, value = "Restart migration execution")
    public String restart() throws Exception {
        var props = new Properties();
        props.setProperty("restartedAt", Long.toString(System.currentTimeMillis()));
        long execId = jobOperator.start("rdbmsToMongoJob", props);
        return "Restarted executionId=" + execId;
    }

    @ShellMethod(key = "restart-last", value = "Restart last failed execution")
    public String restartLast() throws Exception {
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

    @ShellMethod(key = {"mongo-clean", "mclean", "mc"}, value = "Drop Mongo collections")
    public String mongoClean() {
        var targetCollections = List.of("authors", "genres", "books", "comments", "id_mappings");

        int dropped = 0, skipped = 0;
        for (String c : targetCollections) {
            if (MongoMigrationUtils.collectionExists(mongo, c)) {
                mongo.dropCollection(c);
                dropped++;
            } else {
                skipped++;
            }
        }
        return "Mongo cleaned: dropped=" + dropped + " skipped=" + skipped;
    }
}
