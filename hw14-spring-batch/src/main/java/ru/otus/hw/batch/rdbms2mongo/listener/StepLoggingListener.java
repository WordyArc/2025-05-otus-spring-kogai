package ru.otus.hw.batch.rdbms2mongo.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

@Slf4j
@RequiredArgsConstructor
public class StepLoggingListener implements StepExecutionListener {

    private final String stepAlias;

    @Override
    public void beforeStep(StepExecution step) {
        log.info("[{}] STEP START name={} execId={}",
                stepAlias, step.getStepName(), step.getJobExecutionId());
    }

    @Override
    public ExitStatus afterStep(StepExecution step) {
        log.info("[{}] STEP END   status={} read={} written={} filtered={} skips(R/W)={}/{} commits={} rollbacks={}",
                stepAlias,
                step.getStatus(),
                step.getReadCount(),
                step.getWriteCount(),
                step.getFilterCount(),
                step.getReadSkipCount(),
                step.getWriteSkipCount(),
                step.getCommitCount(),
                step.getRollbackCount());
        return step.getExitStatus();
    }
}