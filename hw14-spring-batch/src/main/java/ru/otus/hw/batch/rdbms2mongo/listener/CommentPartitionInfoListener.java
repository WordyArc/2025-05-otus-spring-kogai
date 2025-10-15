package ru.otus.hw.batch.rdbms2mongo.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

@Slf4j
public class CommentPartitionInfoListener implements StepExecutionListener {
    @Override
    public void beforeStep(StepExecution step) {
        var ctx = step.getExecutionContext();
        log.info("[comments] PARTITION START partition={} of {}",
                ctx.getInt("remainder", -1),
                ctx.getInt("modulus", -1));
    }

    @Override
    public ExitStatus afterStep(StepExecution step) {
        var ctx = step.getExecutionContext();
        log.info("[comments] PARTITION END   partition={} of {}  read={} written={} commits={}",
                ctx.getInt("remainder", -1),
                ctx.getInt("modulus", -1),
                step.getReadCount(),
                step.getWriteCount(),
                step.getCommitCount());
        return step.getExitStatus();
    }
}
