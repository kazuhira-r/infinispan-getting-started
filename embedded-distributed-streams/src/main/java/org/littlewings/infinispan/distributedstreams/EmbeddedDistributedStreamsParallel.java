package org.littlewings.infinispan.distributedstreams;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.infinispan.CacheStream;
import org.infinispan.stream.CacheCollectors;

public class EmbeddedDistributedStreamsParallel extends EmbeddedCacheSupport {
    public static void main(String... args) {
        EmbeddedDistributedStreamsParallel eds = new EmbeddedDistributedStreamsParallel();

        switch (args[0]) {
            case "dist-simple":
                eds.distSimple();
                break;
            case "dist-parallel":
                eds.distParallel();
                break;
            case "dist-parallelDistribution":
                eds.distParallelDistribution();
                break;
            case "dist-sequential":
                eds.distSequential();
                break;
            case "dist-sequentialDistribution":
                eds.distSequentialDistribution();
                break;
            case "repl-simple":
                eds.replSimple();
                break;
            case "repl-parallel":
                eds.replParallel();
                break;
            case "repl-parallelDistribution":
                eds.replParallelDistribution();
                break;
            default:
                System.out.printf("unknown option[%s].%n", args[0]);
                System.exit(1);
        }
    }

    public void distSimple() {
        this.<String, String>withCache("distCache", cache -> {
            System.out.println("Cache[" + cache.getName() + "] start.");

            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, "value" + i));

            try (CacheStream<Map.Entry<String, String>> stream = cache.entrySet().stream()) {
                int result =
                        stream
                                .map((Serializable & Function<Map.Entry<String, String>, Integer>) e -> {
                                    System.out.println("map phase Thread[" + Thread.currentThread() + "]");
                                    return Integer.parseInt(e.getValue().substring("value".length()));
                                })
                                .collect(CacheCollectors.serializableCollector(() -> Collectors.summingInt(v -> v)));

                System.out.println("Cache[" + cache.getName() + "] result = " + result);

                int expected = 55;
                if (result != expected) {
                    throw new IllegalStateException("result must be [" + expected + "]");
                }
            }
        });
    }

    public void distParallel() {
        this.<String, String>withCache("distCache", cache -> {
            System.out.println("Cache[" + cache.getName() + "] start.");

            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, "value" + i));

            try (CacheStream<Map.Entry<String, String>> stream = cache.entrySet().stream()) {
                int result =
                        stream
                                .parallel()
                                .map((Serializable & Function<Map.Entry<String, String>, Integer>) e -> {
                                    System.out.println("map phase Thread[" + Thread.currentThread() + "]");
                                    return Integer.parseInt(e.getValue().substring("value".length()));
                                })
                                .collect(CacheCollectors.serializableCollector(() -> Collectors.summingInt(v -> v)));

                System.out.println("Cache[" + cache.getName() + "] result = " + result);

                int expected = 55;
                if (result != expected) {
                    throw new IllegalStateException("result must be [" + expected + "]");
                }
            }
        });
    }

    public void distParallelDistribution() {
        this.<String, String>withCache("distCache", cache -> {
            System.out.println("Cache[" + cache.getName() + "] start.");

            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, "value" + i));

            try (CacheStream<Map.Entry<String, String>> stream = cache.entrySet().stream()) {
                int result =
                        stream
                                .parallelDistribution()
                                .map((Serializable & Function<Map.Entry<String, String>, Integer>) e -> {
                                    System.out.println("map phase Thread[" + Thread.currentThread() + "]");
                                    return Integer.parseInt(e.getValue().substring("value".length()));
                                })
                                .collect(CacheCollectors.serializableCollector(() -> Collectors.summingInt(v -> v)));

                System.out.println("Cache[" + cache.getName() + "] result = " + result);

                int expected = 55;
                if (result != expected) {
                    throw new IllegalStateException("result must be [" + expected + "]");
                }
            }
        });
    }

    public void distSequential() {
        this.<String, String>withCache("distCache", cache -> {
            System.out.println("Cache[" + cache.getName() + "] start.");

            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, "value" + i));

            try (CacheStream<Map.Entry<String, String>> stream = cache.entrySet().stream()) {
                int result =
                        stream
                                .sequential()
                                .map((Serializable & Function<Map.Entry<String, String>, Integer>) e -> {
                                    System.out.println("map phase Thread[" + Thread.currentThread() + "]");
                                    return Integer.parseInt(e.getValue().substring("value".length()));
                                })
                                .collect(CacheCollectors.serializableCollector(() -> Collectors.summingInt(v -> v)));

                System.out.println("Cache[" + cache.getName() + "] result = " + result);

                int expected = 55;
                if (result != expected) {
                    throw new IllegalStateException("result must be [" + expected + "]");
                }
            }
        });
    }

    public void distSequentialDistribution() {
        this.<String, String>withCache("distCache", cache -> {
            System.out.println("Cache[" + cache.getName() + "] start.");

            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, "value" + i));

            try (CacheStream<Map.Entry<String, String>> stream = cache.entrySet().stream()) {
                int result =
                        stream
                                .sequentialDistribution()
                                .map((Serializable & Function<Map.Entry<String, String>, Integer>) e -> {
                                    System.out.println("map phase Thread[" + Thread.currentThread() + "]");
                                    return Integer.parseInt(e.getValue().substring("value".length()));
                                })
                                .collect(CacheCollectors.serializableCollector(() -> Collectors.summingInt(v -> v)));

                System.out.println("Cache[" + cache.getName() + "] result = " + result);

                int expected = 55;
                if (result != expected) {
                    throw new IllegalStateException("result must be [" + expected + "]");
                }
            }
        });
    }

    public void replSimple() {
        this.<String, String>withCache("replCache", cache -> {
            System.out.println("Cache[" + cache.getName() + "] start.");

            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, "value" + i));

            try (CacheStream<Map.Entry<String, String>> stream = cache.entrySet().stream()) {
                int result =
                        stream
                                .map((Serializable & Function<Map.Entry<String, String>, Integer>) e -> {
                                    System.out.println("map phase Thread[" + Thread.currentThread() + "]");
                                    return Integer.parseInt(e.getValue().substring("value".length()));
                                })
                                .collect(CacheCollectors.serializableCollector(() -> Collectors.summingInt(v -> v)));

                System.out.println("Cache[" + cache.getName() + "] result = " + result);

                int expected = 55;
                if (result != expected) {
                    throw new IllegalStateException("result must be [" + expected + "]");
                }
            }
        });
    }

    public void replParallel() {
        this.<String, String>withCache("replCache", cache -> {
            System.out.println("Cache[" + cache.getName() + "] start.");

            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, "value" + i));

            try (CacheStream<Map.Entry<String, String>> stream = cache.entrySet().stream()) {
                int result =
                        stream
                                .map((Serializable & Function<Map.Entry<String, String>, Integer>) e -> {
                                    System.out.println("map phase Thread[" + Thread.currentThread() + "]");
                                    return Integer.parseInt(e.getValue().substring("value".length()));
                                })
                                .collect(CacheCollectors.serializableCollector(() -> Collectors.summingInt(v -> v)));

                System.out.println("Cache[" + cache.getName() + "] result = " + result);

                int expected = 55;
                if (result != expected) {
                    throw new IllegalStateException("result must be [" + expected + "]");
                }
            }
        });
    }

    public void replParallelDistribution() {
        this.<String, String>withCache("replCache", cache -> {
            System.out.println("Cache[" + cache.getName() + "] start.");

            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, "value" + i));

            try (CacheStream<Map.Entry<String, String>> stream = cache.entrySet().stream()) {
                int result =
                        stream
                                .map((Serializable & Function<Map.Entry<String, String>, Integer>) e -> {
                                    System.out.println("map phase Thread[" + Thread.currentThread() + "]");
                                    return Integer.parseInt(e.getValue().substring("value".length()));
                                })
                                .collect(CacheCollectors.serializableCollector(() -> Collectors.summingInt(v -> v)));

                System.out.println("Cache[" + cache.getName() + "] result = " + result);

                int expected = 55;
                if (result != expected) {
                    throw new IllegalStateException("result must be [" + expected + "]");
                }
            }
        });
    }
}
