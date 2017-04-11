package org.littlewings.infinispan.task;

import org.infinispan.tasks.ServerTask;
import org.infinispan.tasks.TaskContext;

public class HelloTask implements ServerTask<String> {
    @Override
    public void setTaskContext(TaskContext taskContext) {
        // no-op
    }

    @Override
    public String getName() {
        return "hello-task";
    }

    @Override
    public String call() throws Exception {
        return "Hello World!!";
    }
}
