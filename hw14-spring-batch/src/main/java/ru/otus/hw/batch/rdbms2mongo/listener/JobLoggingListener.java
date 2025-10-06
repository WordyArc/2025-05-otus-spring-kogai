package ru.otus.hw.batch.rdbms2mongo.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;

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
        int read = 0, written = 0, filtered = 0, rSkips = 0, wSkips = 0, commits = 0, rollbacks = 0;
        for (StepExecution s : job.getStepExecutions()) {
            read += s.getReadCount();
            written += s.getWriteCount();
            filtered += s.getFilterCount();
            rSkips += s.getReadSkipCount();
            wSkips += s.getWriteSkipCount();
            commits += s.getCommitCount();
            rollbacks += s.getRollbackCount();
        }
        log.info("JOB END   name={} id={} status={} exitCode={} " +
                        "read={} written={} filtered={} skips(R/W)={}/{} commits={} rollbacks={}",
                job.getJobInstance().getJobName(),
                job.getId(),
                job.getStatus(),
                job.getExitStatus().getExitCode(),
                read, written, filtered, rSkips, wSkips, commits, rollbacks);
    }
}
