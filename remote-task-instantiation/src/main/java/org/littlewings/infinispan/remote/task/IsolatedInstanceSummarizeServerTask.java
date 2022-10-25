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
import org.infinispan.tasks.TaskInstantiationMode;

public class IsolatedInstanceSummarizeServerTask implements ServerTask<String> {
    AtomicInteger callCounter = new AtomicInteger();

    TaskContext taskContext;

    @Override
    public void setTaskContext(TaskContext taskContext) {
        this.taskContext = taskContext;
    }

    @Override
    public String call() throws Exception {
        Map<String, Object> parameters = taskContext.getParameters().orElse(Collections.emptyMap());

        int called = callCounter.incrementAndGet();

        @SuppressWarnings("unchecked")
        Cache<String, Integer> cache = (Cache<String, Integer>) taskContext.getCache().get();
        int sum = cache.values().stream().collect(CacheCollectors.serializableCollector(() -> Collectors.summingInt(i -> i)));

        return String.format("%s, isolated instance server task call count = %d, sum result = %d", parameters.get("message"), called, sum);
    }

    @Override
    public String getName() {
        return IsolatedInstanceSummarizeServerTask.class.getSimpleName();
    }

    @Override
    public TaskExecutionMode getExecutionMode() {
        return TaskExecutionMode.ALL_NODES;
    }

    @Override
    public TaskInstantiationMode getInstantiationMode() {
        return TaskInstantiationMode.ISOLATED;
    }
}
