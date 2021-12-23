package org.littlewings.infinispan.remote.health;

import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.RemoteCacheManagerAdmin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClusterHealthApiTest {
    static String createUri(String userName, String password) {
        return String.format("hotrod://%s:%s@172.19.0.2:11222,172.19.0.3:11222,172.19.0.4:11222", userName, password);
    }

    @BeforeAll
    public static void setupAll() {
        String uri = createUri("admin-user", "password");

        try (RemoteCacheManager manager = new RemoteCacheManager(uri)) {
            RemoteCacheManagerAdmin admin = manager.administration();

            org.infinispan.configuration.cache.Configuration distCacheConfiguration =
                    new org.infinispan.configuration.cache.ConfigurationBuilder()
                            .clustering()
                            .cacheMode(org.infinispan.configuration.cache.CacheMode.DIST_SYNC)
                            .encoding().key().mediaType("application/x-protostream")
                            .encoding().value().mediaType("application/x-protostream")
                            .build();

            admin.getOrCreateCache("distCache", distCacheConfiguration);
        }
    }

    <K, V> void withCache(String cacheName, Consumer<RemoteCache<K, V>> func) {
        String uri = createUri("app-user", "password");

        try (RemoteCacheManager manager = new RemoteCacheManager(uri)) {
            RemoteCache<K, V> cache = manager.getCache(cacheName);

            func.accept(cache);
        }
    }

    @Test
    public void putsData() {
        this.<String, String>withCache("distCache", cache -> {
            int targetEntries = 1000000;

            IntStream
                    .rangeClosed(1, targetEntries)
                    .parallel()
                    .peek(i -> {
                        if (i % 100000 == 0) {
                            System.out.printf("[%s] putted, %d entries%n", LocalDateTime.now(), i);
                        }
                    })
                    .forEach(i -> cache.put("key" + i, "value" + i));

            assertThat(cache.size()).isEqualTo(targetEntries);
        });
    }
}
