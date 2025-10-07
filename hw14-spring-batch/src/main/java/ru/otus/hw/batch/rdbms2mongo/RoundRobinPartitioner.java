package ru.otus.hw.batch.rdbms2mongo;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.LinkedHashMap;
import java.util.Map;

/**
* Суть просто, берём число партиций P (обычно = числу рабочих потоков).
* Каждая партиция i (0…P-1) обрабатывает ровно те строки, где id % P == i.
* */
public class RoundRobinPartitioner implements Partitioner {

    public static final String MODULUS_KEY = "modulus";

    public static final String REMAINDER_KEY = "remainder";

    private final int partitions;

    public RoundRobinPartitioner(int partitions) {
        this.partitions = partitions;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        final int n = partitions > 0 ? partitions : Math.max(1, gridSize);

        Map<String, ExecutionContext> partitionContexts = new LinkedHashMap<>(n);
        for (int i = 0; i < n; i++) {
            var ctx = new ExecutionContext();
            ctx.putInt(MODULUS_KEY, n);
            ctx.putInt(REMAINDER_KEY, i);

            partitionContexts.put("p" + i, ctx);
        }
        return partitionContexts;
    }
}
