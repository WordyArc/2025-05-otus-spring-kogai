package ru.otus.hw.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "app.batch")
public class BatchProperties {

    private IdMappingCache idMappingCache = new IdMappingCache();

    private ThreadPool threadPool = new ThreadPool();

    private ChunkSizes chunkSizes = new ChunkSizes();

    private ProgressListeners progressListeners = new ProgressListeners();

    @Data
    public static class IdMappingCache {
        private long maximumSize = 500_000;
        private Map<String, Long> perType = new HashMap<>();
    }

    @Data
    public static class ThreadPool {
        private String threadNamePrefix = "batch-";
        private int corePoolSize = 4;
        private int maxPoolSize = 8;
        private int queueCapacity = 0;
    }

    @Data
    public static class ChunkSizes {
        private int authors = 500;
        private int genres = 1000;
        private int books = 200;
        private int comments = 5000;
    }

    @Data
    public static class ProgressListeners {
        private int authors = 5_000;
        private int genres = 200;
        private int books = 10_000;
        private int comments = 20_000;
    }
}
