package org.littlewings.infinispan.remote.metrics;

import java.util.stream.IntStream;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.RemoteCacheManagerAdmin;
import org.infinispan.client.hotrod.ServerStatistics;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.jmx.RemoteCacheClientStatisticsMXBean;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MetricsTest {
    static String createUri(String userName, String password) {
        return String.format("hotrod://%s:%s@172.18.0.2:11222,172.18.0.3:11222,172.18.0.4:11222", userName, password);
    }

    @BeforeAll
    static void setUpAll() {
        String uri = createUri("ispn-admin", "password");

        try (RemoteCacheManager manager = new RemoteCacheManager(uri)) {
            RemoteCacheManagerAdmin admin = manager.administration();

            org.infinispan.configuration.cache.Configuration distCacheConfiguration =
                    new org.infinispan.configuration.cache.ConfigurationBuilder()
                            .clustering()
                            .cacheMode(org.infinispan.configuration.cache.CacheMode.DIST_SYNC)
                            .encoding().key().mediaType("application/x-protostream")
                            .encoding().value().mediaType("application/x-protostream")
                            .statistics().enable()
                            .build();

            admin.getOrCreateCache("distCache", distCacheConfiguration);

            org.infinispan.configuration.cache.Configuration replCacheConfiguration =
                    new org.infinispan.configuration.cache.ConfigurationBuilder()
                            .clustering()
                            .cacheMode(org.infinispan.configuration.cache.CacheMode.REPL_SYNC)
                            .encoding().key().mediaType("application/x-protostream")
                            .encoding().value().mediaType("application/x-protostream")
                            .statistics().enable()
                            .build();

            admin.getOrCreateCache("replCache", replCacheConfiguration);
        }
    }

    @Test
    public void distributedCache() {
        String uri = createUri("ispn-user", "password");

        try (RemoteCacheManager manager = new RemoteCacheManager(uri)) {
            RemoteCache<String, String> cache = manager.getCache("distCache");

            IntStream
                    .rangeClosed(1, 100)
                    .forEach(i -> cache.put("key" + i, "value" + i));

            IntStream
                    .rangeClosed(1, 50)
                    .forEach(i -> assertThat(cache.get("key" + i)).isNotNull());

            IntStream
                    .rangeClosed(101, 125)
                    .forEach(i -> assertThat(cache.get("key" + i)).isNull());
        }
    }

    @Test
    public void replicatedCache() {
        String uri = createUri("ispn-user", "password");

        try (RemoteCacheManager manager = new RemoteCacheManager(uri)) {
            RemoteCache<String, String> cache = manager.getCache("replCache");

            IntStream
                    .rangeClosed(1, 100)
                    .forEach(i -> cache.put("key" + i, "value" + i));

            IntStream
                    .rangeClosed(1, 50)
                    .forEach(i -> assertThat(cache.get("key" + i)).isNotNull());

            IntStream
                    .rangeClosed(101, 125)
                    .forEach(i -> assertThat(cache.get("key" + i)).isNull());
        }
    }

    @Test
    public void clientMetrics() {
        String uri = createUri("ispn-user", "password");

        Configuration configuration =
                new ConfigurationBuilder()
                        .uri(uri)
                        .statistics().enable() // .jmxEnable() JMX MBeanも有効にする場合
                        .build();

        try (RemoteCacheManager manager = new RemoteCacheManager(configuration)) {
            RemoteCache<String, String> cache = manager.getCache("distCache");

            IntStream
                    .rangeClosed(1, 100)
                    .forEach(i -> cache.put("key" + i, "value" + i));

            IntStream
                    .rangeClosed(1, 50)
                    .forEach(i -> assertThat(cache.get("key" + i)).isNotNull());

            IntStream
                    .rangeClosed(101, 125)
                    .forEach(i -> assertThat(cache.get("key" + i)).isNull());

            ServerStatistics serverStatistics = cache.serverStatistics();

            serverStatistics
                    .getStatsMap()
                    .entrySet().forEach(entry -> System.out.printf("%s = %s%n", entry.getKey(), entry.getValue()));

            RemoteCacheClientStatisticsMXBean clientStatistics = cache.clientStatistics();
            assertThat(clientStatistics.getRemoteStores()).isEqualTo(100);
            assertThat(clientStatistics.getRemoteHits()).isEqualTo(50);
            assertThat(clientStatistics.getRemoteMisses()).isEqualTo(25);
        }
    }
}
