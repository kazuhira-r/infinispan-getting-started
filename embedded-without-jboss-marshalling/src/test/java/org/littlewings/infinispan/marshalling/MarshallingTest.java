package org.littlewings.infinispan.marshalling;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MarshallingTest {
    public <K, V> void withCache(String cacheName, int numInstances, Consumer<Cache<K, V>> func) {
        List<EmbeddedCacheManager> cacheManagers =
                IntStream
                        .rangeClosed(1, numInstances)
                        .mapToObj(i -> {
                            try {
                                return new DefaultCacheManager("infinispan.xml");
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        })
                        .collect(Collectors.toList());

        cacheManagers.forEach(m -> m.getCache(cacheName));

        try {
            Cache<K, V> cache = cacheManagers.get(0).getCache(cacheName);
            func.accept(cache);
            cache.stop();
        } finally {
            cacheManagers.forEach(EmbeddedCacheManager::stop);
        }
    }

    @Test
    public void simplyClassLocalCache() {
        this.<String, String>withCache("localCache", 1, cache -> {
            cache.put("key1", "value1");
            cache.put("key2", "value2");
            cache.put("key3", "value3");

            assertThat(cache.get("key1")).isEqualTo("value1");
            assertThat(cache.get("key2")).isEqualTo("value2");
            assertThat(cache.get("key3")).isEqualTo("value3");
        });
    }

    @Test
    public void simplyClassDistributedCache() {
        this.<String, String>withCache("distributedCache", 1, cache -> {
            cache.put("key1", "value1");
            cache.put("key2", "value2");
            cache.put("key3", "value3");

            assertThat(cache.get("key1")).isEqualTo("value1");
            assertThat(cache.get("key2")).isEqualTo("value2");
            assertThat(cache.get("key3")).isEqualTo("value3");
        });
    }

    @Test
    public void simplyClassClusteredLocalCache() {
        this.<String, String>withCache("localCache", 3, cache -> {
            cache.put("key1", "value1");
            cache.put("key2", "value2");
            cache.put("key3", "value3");

            assertThat(cache.get("key1")).isEqualTo("value1");
            assertThat(cache.get("key2")).isEqualTo("value2");
            assertThat(cache.get("key3")).isEqualTo("value3");
        });
    }

    @Test
    public void simplyClassClusteredDistributedCache() {
        this.<String, String>withCache("distributedCache", 3, cache -> {
            cache.put("key1", "value1");
            cache.put("key2", "value2");
            cache.put("key3", "value3");

            assertThat(cache.get("key1")).isEqualTo("value1");
            assertThat(cache.get("key2")).isEqualTo("value2");
            assertThat(cache.get("key3")).isEqualTo("value3");
        });
    }

    @Test
    public void userDefinedClassLocalCache() {
        this.<String, Book>withCache("localCache", 1, cache -> {
            List<Book> books =
                    Arrays
                            .asList(
                                    Book.create("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 5337),
                                    Book.create("978-1785285332", "Getting Started With Hazelcast - Second Edition", 3848),
                                    Book.create("978-1783988181", "Mastering Redis", 6172)
                            );

            books.forEach(b -> cache.put(b.getIsbn(), b));

            assertThat(cache.get("978-1782169970").getTitle()).isEqualTo("Infinispan Data Grid Platform Definitive Guide");
            assertThat(cache.get("978-1785285332").getTitle()).isEqualTo("Getting Started With Hazelcast - Second Edition");
            assertThat(cache.get("978-1783988181").getTitle()).isEqualTo("Mastering Redis");
        });
    }

    @Test
    public void userDefinedClassDistributedCache() {
        this.<String, Book>withCache("distributedCache", 1, cache -> {
            List<Book> books =
                    Arrays
                            .asList(
                                    Book.create("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 5337),
                                    Book.create("978-1785285332", "Getting Started With Hazelcast - Second Edition", 3848),
                                    Book.create("978-1783988181", "Mastering Redis", 6172)
                            );

            books.forEach(b -> cache.put(b.getIsbn(), b));

            assertThat(cache.get("978-1782169970").getTitle()).isEqualTo("Infinispan Data Grid Platform Definitive Guide");
            assertThat(cache.get("978-1785285332").getTitle()).isEqualTo("Getting Started With Hazelcast - Second Edition");
            assertThat(cache.get("978-1783988181").getTitle()).isEqualTo("Mastering Redis");
        });
    }

    @Test
    public void userDefinedClassClusteredLocalCache() {
        this.<String, Book>withCache("localCache", 3, cache -> {
            List<Book> books =
                    Arrays
                            .asList(
                                    Book.create("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 5337),
                                    Book.create("978-1785285332", "Getting Started With Hazelcast - Second Edition", 3848),
                                    Book.create("978-1783988181", "Mastering Redis", 6172)
                            );

            books.forEach(b -> cache.put(b.getIsbn(), b));

            assertThat(cache.get("978-1782169970").getTitle()).isEqualTo("Infinispan Data Grid Platform Definitive Guide");
            assertThat(cache.get("978-1785285332").getTitle()).isEqualTo("Getting Started With Hazelcast - Second Edition");
            assertThat(cache.get("978-1783988181").getTitle()).isEqualTo("Mastering Redis");
        });
    }

    @Test
    public void userDefinedClassClusteredDistributedCache() {
        this.<String, Book>withCache("distributedCache", 3, cache -> {
            List<Book> books =
                    Arrays
                            .asList(
                                    Book.create("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 5337),
                                    Book.create("978-1785285332", "Getting Started With Hazelcast - Second Edition", 3848),
                                    Book.create("978-1783988181", "Mastering Redis", 6172)
                            );

            books.forEach(b -> cache.put(b.getIsbn(), b));

            assertThat(cache.get("978-1782169970").getTitle()).isEqualTo("Infinispan Data Grid Platform Definitive Guide");
            assertThat(cache.get("978-1785285332").getTitle()).isEqualTo("Getting Started With Hazelcast - Second Edition");
            assertThat(cache.get("978-1783988181").getTitle()).isEqualTo("Mastering Redis");
        });
    }
}
