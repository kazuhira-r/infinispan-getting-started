package org.littlewings.infinispan.remote.task;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.RemoteCacheManagerAdmin;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.configuration.cache.CacheMode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RemoteTaskInstantiationTest {
    @BeforeAll
    static void createCache() {
        Configuration configuration =
                new ConfigurationBuilder()
                        .uri("hotrod://ispn-admin:password@172.18.0.2:11222,172.18.0.3:11222,172.18.0.4:11222")
                        .build();

        try (RemoteCacheManager manager = new RemoteCacheManager(configuration)) {
            RemoteCacheManagerAdmin admin = manager.administration();

            admin.removeCache("distCache");

            org.infinispan.configuration.cache.Configuration cacheConfiguration =
                    new org.infinispan.configuration.cache.ConfigurationBuilder()
                            .clustering().cacheMode(CacheMode.DIST_SYNC)
                            .security().authorization().enable()
                            .encoding().key().mediaType("application/x-protostream")
                            .encoding().value().mediaType("application/x-protostream")
                            .build();
            admin.getOrCreateCache("distCache", cacheConfiguration);
        }
    }

    <K, V> void withRemoteCache(String cacheName, Consumer<RemoteCache<K, V>> consumer) {
        Configuration configuration =
                new ConfigurationBuilder()
                        .uri("hotrod://ispn-user:password@172.18.0.2:11222,172.18.0.3:11222,172.18.0.4:11222")
                        .build();


        try (RemoteCacheManager manager = new RemoteCacheManager(configuration)) {
            RemoteCache<K, V> cache = manager.getCache(cacheName);

            consumer.accept(cache);
        }
    }

    @Test
    public void instanceSharedTasks() {
        this.<String, Integer>withRemoteCache("distCache", cache -> {
            IntStream.rangeClosed(1, 100).forEach(i -> cache.put("key" + i, i));

            assertThat(cache).hasSize(100);

            List<String> results1 =
                    cache.execute("SharedInstanceSummarizeServerTask", Map.of("message", "hello"));

            assertThat(results1)
                    .hasSize(3)
                    .containsOnly(
                            "hello, shared instance server task call count = 1, sum result = 5050",
                            "hello, shared instance server task call count = 1, sum result = 5050",
                            "hello, shared instance server task call count = 1, sum result = 5050"
                    );

            List<String> results2 =
                    cache.execute("SharedInstanceSummarizeServerTask", Map.of("message", "world"));

            assertThat(results2)
                    .hasSize(3)
                    .containsOnly(
                            "world, shared instance server task call count = 2, sum result = 5050",
                            "world, shared instance server task call count = 2, sum result = 5050",
                            "world, shared instance server task call count = 2, sum result = 5050"
                    );

            List<String> results3 =
                    cache.execute("SharedInstanceSummarizeServerTask", Map.of("message", "yeah"));

            assertThat(results3)
                    .hasSize(3)
                    .containsOnly(
                            "yeah, shared instance server task call count = 3, sum result = 5050",
                            "yeah, shared instance server task call count = 3, sum result = 5050",
                            "yeah, shared instance server task call count = 3, sum result = 5050"
                    );

            cache.clear();
        });
    }

    @Test
    public void instanceIsolatedTasks() {
        this.<String, Integer>withRemoteCache("distCache", cache -> {
            IntStream.rangeClosed(1, 100).forEach(i -> cache.put("key" + i, i));

            assertThat(cache).hasSize(100);

            List<String> results1 =
                    cache.execute("IsolatedInstanceSummarizeServerTask", Map.of("message", "hello"));

            assertThat(results1)
                    .hasSize(3)
                    .containsOnly(
                            "hello, isolated instance server task call count = 1, sum result = 5050",
                            "hello, isolated instance server task call count = 1, sum result = 5050",
                            "hello, isolated instance server task call count = 1, sum result = 5050"
                    );

            List<String> results2 =
                    cache.execute("IsolatedInstanceSummarizeServerTask", Map.of("message", "world"));

            assertThat(results2)
                    .hasSize(3)
                    .containsOnly(
                            "world, isolated instance server task call count = 1, sum result = 5050",
                            "world, isolated instance server task call count = 1, sum result = 5050",
                            "world, isolated instance server task call count = 1, sum result = 5050"
                    );

            List<String> results3 =
                    cache.execute("IsolatedInstanceSummarizeServerTask", Map.of("message", "yeah"));

            assertThat(results3)
                    .hasSize(3)
                    .containsOnly(
                            "yeah, isolated instance server task call count = 1, sum result = 5050",
                            "yeah, isolated instance server task call count = 1, sum result = 5050",
                            "yeah, isolated instance server task call count = 1, sum result = 5050"
                    );

            cache.clear();
        });
    }
}
