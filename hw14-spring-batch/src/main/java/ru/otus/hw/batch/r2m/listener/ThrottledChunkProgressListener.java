package ru.otus.hw.batch.r2m.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.listener.ChunkListenerSupport;

@Slf4j
@RequiredArgsConstructor
public class ThrottledChunkProgressListener extends ChunkListenerSupport {

    private final String stepAlias;
    private final int stride;
    private int lastLogged = 0;

    @Override
    public void afterChunk(ChunkContext context) {
        StepExecution se = context.getStepContext().getStepExecution();
        int read = Math.toIntExact(se.getReadCount());
        if (read - lastLogged >= stride) {
            log.info("[{}] progress: read={} written={} filter={} commits={}",
                    stepAlias, read, se.getWriteCount(), se.getFilterCount(), se.getCommitCount());
            lastLogged = read;
        }
    }
}