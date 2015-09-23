package org.littlewings.infinispan.distributedstreams;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.infinispan.CacheStream;
import org.infinispan.stream.CacheCollectors;

public class EmbeddedDistributedStreamsSimple extends EmbeddedCacheSupport {
    public static void main(String... args) {
        EmbeddedDistributedStreamsSimple eds = new EmbeddedDistributedStreamsSimple();
        eds.executeDistributedStreams();
    }

    public void executeDistributedStreams() {
        this.<String, String>withCache("localCache", cache -> {
            System.out.println("Cache[" + cache.getName() + "] start.");

            IntStream.rangeClosed(1, 100).forEach(i -> cache.put("key" + i, "value" + i));

            try (CacheStream<Map.Entry<String, String>> stream = cache.entrySet().stream()) {
                System.out.println(stream.getClass());

                int result =
                        stream
                                .map(e -> Integer.parseInt(e.getValue().substring("value".length())))
                                .collect(Collectors.summingInt(v -> v));

                System.out.println("Cache[" + cache.getName() + "] result = " + result);

                int expected = 5050;
                if (result != expected) {
                    throw new IllegalStateException("result must be [" + expected + "]");
                }
            }
        });

        this.<String, String>withCache("distCache", 3, cache -> {
            System.out.println("Cache[" + cache.getName() + "] start.");

            IntStream.rangeClosed(1, 100).forEach(i -> cache.put("key" + i, "value" + i));

            try (CacheStream<Map.Entry<String, String>> stream = cache.entrySet().stream()) {
                System.out.println(stream.getClass());

                int result =
                        stream
                                .map((Serializable & Function<Map.Entry<String, String>, Integer>) e -> {
                                    return Integer.parseInt(e.getValue().substring("value".length()));
                                })
                                .collect(CacheCollectors.serializableCollector(() -> Collectors.summingInt(v -> v)));

                System.out.println("Cache[" + cache.getName() + "] result = " + result);

                int expected = 5050;
                if (result != expected) {
                    throw new IllegalStateException("result must be [" + expected + "]");
                }
            }
        });

        this.<String, String>withCache("replCache", 3, cache -> {
            System.out.println("Cache[" + cache.getName() + "] start.");

            IntStream.rangeClosed(1, 100).forEach(i -> cache.put("key" + i, "value" + i));

            try (CacheStream<Map.Entry<String, String>> stream = cache.entrySet().stream()) {
                System.out.println(stream.getClass());

                /*
                int result =
                        stream
                                .map((Serializable & Function<Map.Entry<String, String>, Integer>) e -> {
                                    return Integer.parseInt(e.getValue().substring("value".length()));
                                })
                                .collect(CacheCollectors.serializableCollector(() -> Collectors.summingInt(v -> v)));
                                */
                int result =
                        stream
                                .map(e -> Integer.parseInt(e.getValue().substring("value".length())))
                                .collect(Collectors.summingInt(v -> v));

                System.out.println("Cache[" + cache.getName() + "] result = " + result);

                int expected = 5050;
                if (result != expected) {
                    throw new IllegalStateException("result must be [" + expected + "]");
                }
            }
        });

        this.<String, String>withCache("invalCache", 3, cache -> {
            System.out.println("Cache[" + cache.getName() + "] start.");

            IntStream.rangeClosed(1, 100).forEach(i -> cache.put("key" + i, "value" + i));

            try (CacheStream<Map.Entry<String, String>> stream = cache.entrySet().stream()) {
                System.out.println(stream.getClass());

                /*
                int result =
                        stream
                                .map((Serializable & Function<Map.Entry<String, String>, Integer>) e -> {
                                    return Integer.parseInt(e.getValue().substring("value".length()));
                                })
                                .collect(CacheCollectors.serializableCollector(() -> Collectors.summingInt(v -> v)));
                                */
                int result =
                        stream
                                .map(e -> Integer.parseInt(e.getValue().substring("value".length())))
                                .collect(Collectors.summingInt(v -> v));

                System.out.println("Cache[" + cache.getName() + "] result = " + result);

                int expected = 5050;
                if (result != expected) {
                    throw new IllegalStateException("result must be [" + expected + "]");
                }
            }
        });

        this.<String, String>withCache("distCache", 3, cache -> {
            System.out.println("Cache[" + cache.getName() + "] start.");

            IntStream.rangeClosed(1, 100).forEach(i -> cache.put("key" + i, "value" + i));

            try (CacheStream<Map.Entry<String, String>> stream = cache.entrySet().stream()) {
                System.out.println(stream.getClass());

                int result =
                        stream
                                .filter((Serializable & Predicate<Map.Entry<String, String>>) e -> {
                                    return Integer.parseInt(e.getKey().substring("key".length())) % 2 == 0;
                                })
                                .map((Serializable & Function<Map.Entry<String, String>, Integer>) e -> {
                                    return Integer.parseInt(e.getValue().substring("value".length()));
                                })
                                .collect(CacheCollectors.serializableCollector(() -> Collectors.summingInt(v -> v)));

                System.out.println("Cache[" + cache.getName() + "] result = " + result);

                int expected = 2550;
                if (result != expected) {
                    throw new IllegalStateException("result must be [" + expected + "]");
                }
            }
        });

        this.<String, String>withCache("distCache", 3, cache -> {
            System.out.println("Cache[" + cache.getName() + "] start.");

            IntStream.rangeClosed(1, 100).forEach(i -> cache.put("key" + i, "value" + i));

            try (CacheStream<Map.Entry<String, String>> stream = cache.entrySet().stream()) {
                System.out.println(stream.getClass());

                Set<String> keys = new HashSet<>();
                keys.add("key1");
                keys.add("key10");

                int result =
                        stream
                                .filterKeys(keys)
                                .map((Serializable & Function<Map.Entry<String, String>, Integer>) e -> {
                                    return Integer.parseInt(e.getValue().substring("value".length()));
                                })
                                .collect(CacheCollectors.serializableCollector(() -> Collectors.summingInt(v -> v)));

                System.out.println("Cache[" + cache.getName() + "] result = " + result);

                int expected = 11;
                if (result != expected) {
                    throw new IllegalStateException("result must be [" + expected + "]");
                }
            }
        });
    }
}
