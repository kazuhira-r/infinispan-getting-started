package org.littlewings.infinispan.marshalling;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MarshallingTest {
    @BeforeEach
    public void setUp() {
        Configuration configuration =
                new ConfigurationBuilder()
                        .addServers("172.17.0.2:11222")
                        .build();

        RemoteCacheManager manager = new RemoteCacheManager(configuration);

        manager.getCache("simpleCache").clear();
        manager.getCache("bookCache").clear();
    }

    <K, V> void withCache(String cacheName, Consumer<RemoteCache<K, V>> consumer) {
        Configuration configuration =
                new ConfigurationBuilder()
                        .addServers("172.17.0.2:11222")
                        //.addContextInitializer(new BookContextInitializerImpl())
                        .addContextInitializer("org.littlewings.infinispan.marshalling.BookContextInitializerImpl")
                        .build();

        RemoteCacheManager manager = new RemoteCacheManager(configuration);

        RemoteCache<K, V> cache = manager.getCache(cacheName);

        try {
            consumer.accept(cache);
        } finally {
            cache.stop();
            manager.stop();
        }
    }

    @Test
    public void simpleCase() {
        this.<String, String>withCache("simpleCache", cache -> {
            cache.put("key1", "value1");
            cache.put("key2", "value2");
            cache.put("key3", "value3");

            assertThat(cache.get("key1")).isEqualTo("value1");
            assertThat(cache.get("key2")).isEqualTo("value2");
            assertThat(cache.get("key3")).isEqualTo("value3");
        });
    }

    @Test
    public void useDefinedClassCase() {
        this.<String, Book>withCache("bookCache", cache -> {
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
