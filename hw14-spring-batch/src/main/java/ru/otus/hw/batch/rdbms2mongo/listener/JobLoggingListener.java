package ru.otus.hw.batch.rdbms2mongo.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;

import java.util.Collection;
import java.util.function.ToLongFunction;

@Slf4j
public class JobLoggingListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution job) {
        log.info("JOB START name={} id={} params={}",
                job.getJobInstance().getJobName(),
                job.getId(),
                job.getJobParameters());
    }

    @Override
    public void afterJob(JobExecution job) {
        var steps = job.getStepExecutions();

        log.info("JOB END name={} id={} status={} exitCode={} " +
                        "read={} written={} filtered={} skips(R/W)={}/{} commits={} rollbacks={}",
                job.getJobInstance().getJobName(),
                job.getId(),
                job.getStatus(),
                job.getExitStatus().getExitCode(),
                sum(steps, StepExecution::getReadCount),
                sum(steps, StepExecution::getWriteCount),
                sum(steps, StepExecution::getFilterCount),
                sum(steps, StepExecution::getReadSkipCount),
                sum(steps, StepExecution::getWriteSkipCount),
                sum(steps, StepExecution::getCommitCount),
                sum(steps, StepExecution::getRollbackCount));
    }

    private long sum(Collection<StepExecution> steps, ToLongFunction<StepExecution> mapper) {
        return steps.stream().mapToLong(mapper).sum();
    }
}
