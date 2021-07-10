package org.littlewings.infinispan.task;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.infinispan.Cache;
import org.infinispan.tasks.ServerTask;
import org.infinispan.tasks.TaskContext;
import org.infinispan.tasks.TaskExecutionMode;
import org.jboss.logging.Logger;
import org.littlewings.infinispan.entity.Book;
import org.littlewings.infinispan.entity.Result;

public class PriceSumTask implements ServerTask<Result> {
    TaskContext taskContext;

    @Override
    public void setTaskContext(TaskContext taskContext) {
        Logger logger = Logger.getLogger(PriceSumTask.class);
        logger.infof("set task context");
        this.taskContext = taskContext;
    }

    @Override
    public Result call() throws Exception {
        Logger logger = Logger.getLogger(PriceSumTask.class);
        logger.infof("start task");

        Map<String, ?> parameters = taskContext.getParameters().orElse(Collections.emptyMap());

        int greaterThanPrice;

        if (parameters.containsKey("greaterThanPrice")) {
            greaterThanPrice = (Integer) parameters.get("greaterThanPrice");  // ALL_NODESの時は値はStringのみ
        } else {
            greaterThanPrice = 0;
        }

        Cache<String, Book> cache = (Cache<String, Book>) taskContext.getCache().orElseThrow();

        logger.infof("execution mode = %s, cache size = %d, greaterThan = %d", getExecutionMode(), cache.size(), greaterThanPrice);

        Result result = Result.create(
                cache
                        .values()
                        .stream()
                        .filter(book -> book.getPrice() > greaterThanPrice)
                        .map(book -> {
                            Logger l = Logger.getLogger(PriceSumTask.class);
                            l.infof("map entry = %s", book.getIsbn());
                            return book.getPrice();
                        })
                        .collect(() -> Collectors.summingInt(price -> price))
        );

        logger.infof("end task, result = %d", result.getValue());

        return result;
    }

    @Override
    public String getName() {
        return "price-sum-task";
    }

    @Override
    public TaskExecutionMode getExecutionMode() {
        return TaskExecutionMode.ONE_NODE;  // default, ONE_NODE
    }
}
