package org.littlewings.infinispan.remote.task;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.infinispan.Cache;
import org.infinispan.stream.CacheCollectors;
import org.infinispan.tasks.ServerTask;
import org.infinispan.tasks.TaskContext;
import org.infinispan.tasks.TaskExecutionMode;

public class SharedInstanceSummarizeServerTask implements ServerTask<String> {
    AtomicInteger callCounter = new AtomicInteger();

    ThreadLocal<TaskContext> threadLocalTaskContext = new ThreadLocal<>();

    @Override
    public void setTaskContext(TaskContext taskContext) {
        this.threadLocalTaskContext.set(taskContext);
    }

    @Override
    public String call() throws Exception {
        TaskContext taskContext = threadLocalTaskContext.get();

        try {
            Map<String, Object> parameters = taskContext.getParameters().orElse(Collections.emptyMap());

            int called = callCounter.incrementAndGet();

            @SuppressWarnings("unchecked")
            Cache<String, Integer> cache = (Cache<String, Integer>) taskContext.getCache().get();
            int sum = cache.values().stream().collect(CacheCollectors.serializableCollector(() -> Collectors.summingInt(i -> i)));

            return String.format("%s, shared instance server task call count = %d, sum result = %d", parameters.get("message"), called, sum);
        } finally {
            threadLocalTaskContext.remove();
        }
    }

    @Override
    public String getName() {
        return SharedInstanceSummarizeServerTask.class.getSimpleName();
    }

    @Override
    public TaskExecutionMode getExecutionMode() {
        return TaskExecutionMode.ALL_NODES;
    }

    /* デフォルトで以下と同じ
    @Override
    public TaskInstantiationMode getInstantiationMode() {
        return TaskInstantiationMode.SHARED;
    }
    */
}
