package ru.otus.hw.batch.rdbms2mongo.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;

@Slf4j
public class ThrottledChunkProgressListener implements ChunkListener {

    private final String stepAlias;

    private final int stride;

    private int lastLogged = 0;

    private boolean initialized = false;

    public ThrottledChunkProgressListener(String stepAlias, int stride) {
        this.stepAlias = stepAlias;
        this.stride = stride;
    }

    @Override
    public void beforeChunk(ChunkContext context) {
        if (!initialized) {
            initialized = true;
            lastLogged = 0;
            log.info("[{}] progress-listener started (stride={})", stepAlias, stride);
        }
    }

    @Override
    public void afterChunk(ChunkContext context) {
        StepExecution se = context.getStepContext().getStepExecution();
        int read = Math.toIntExact(se.getReadCount());
        if (read - lastLogged >= stride) {
            log.info("[{}] progress: read={} written={} filtered={} commits={}",
                    stepAlias, read, se.getWriteCount(), se.getFilterCount(), se.getCommitCount());
            lastLogged = read;
        }
    }

    @Override
    public void afterChunkError(ChunkContext context) {
        StepExecution se = context.getStepContext().getStepExecution();
        Object ex = context.getAttribute(ChunkListener.ROLLBACK_EXCEPTION_KEY);
        log.warn("[{}] chunk error: read={} written={} filtered={} commits={} ex={}",
                stepAlias, se.getReadCount(), se.getWriteCount(), se.getFilterCount(),
                se.getCommitCount(), ex);
    }
}
