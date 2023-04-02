package org.littlewings.infinispan.remote.nearcache;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.RemoteCacheManagerAdmin;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.ExhaustedAction;
import org.infinispan.client.hotrod.configuration.NearCacheMode;
import org.infinispan.client.hotrod.impl.InvalidatedNearRemoteCache;
import org.infinispan.client.hotrod.impl.RemoteCacheImpl;
import org.infinispan.client.hotrod.jmx.RemoteCacheClientStatisticsMXBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class NearCacheTest {
    @BeforeEach
    void setUp() {
        String uri = String.format(
                "hotrod://%s:%s@172.18.0.2:11222,172.18.0.3:11222,172.18.0.4:11222",
                "ispn-admin",
                "password"
        );

        try (RemoteCacheManager manager = new RemoteCacheManager(uri)) {
            RemoteCacheManagerAdmin admin = manager.administration();

            admin.removeCache("distCache");

            org.infinispan.configuration.cache.Configuration configuration =
                    new org.infinispan.configuration.cache.ConfigurationBuilder()
                            .clustering()
                            .cacheMode(org.infinispan.configuration.cache.CacheMode.DIST_SYNC)
                            .encoding().key().mediaType("application/x-protostream")
                            .encoding().value().mediaType("application/x-protostream")
                            .build();

            admin.getOrCreateCache("distCache", configuration);
        }
    }

    @Test
    void defaultCache() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServers("172.18.0.2:11222;172.18.0.3:11222;172.18.0.4:11222");
        builder.security().authentication().username("ispn-user").password("password");

        Configuration configuration = builder.build();

        try (RemoteCacheManager cacheManager = new RemoteCacheManager(configuration)) {
            RemoteCache<String, String> cache = cacheManager.getCache("distCache");
            assertThat(cache).isInstanceOf(RemoteCacheImpl.class);
        }
    }

    @Test
    void enableUnboundedNearCache() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServers("172.18.0.2:11222;172.18.0.3:11222;172.18.0.4:11222");
        builder.security().authentication().username("ispn-user").password("password");

        builder.remoteCache("distCache").nearCacheMode(NearCacheMode.INVALIDATED);

        Configuration configuration = builder.build();

        try (RemoteCacheManager cacheManager = new RemoteCacheManager(configuration)) {
            RemoteCache<String, String> cache = cacheManager.getCache("distCache");
            assertThat(cache).isInstanceOf(InvalidatedNearRemoteCache.class);

            RemoteCacheClientStatisticsMXBean clientStatistics = cache.clientStatistics();

            assertThat(clientStatistics.getNearCacheHits()).isZero();
            assertThat(clientStatistics.getNearCacheMisses()).isZero();
            assertThat(clientStatistics.getNearCacheSize()).isZero();
            assertThat(clientStatistics.getNearCacheInvalidations()).isZero();

            IntStream.rangeClosed(1, 100).forEach(i -> cache.put("key" + i, "value1-" + i));

            assertThat(clientStatistics.getNearCacheHits()).isZero();
            assertThat(clientStatistics.getNearCacheMisses()).isZero();
            assertThat(clientStatistics.getNearCacheSize()).isZero();
            assertThat(clientStatistics.getNearCacheInvalidations()).isZero();

            IntStream.rangeClosed(1, 50).forEach(i -> assertThat(cache.get("key" + i)).isEqualTo("value1-" + i));

            assertThat(clientStatistics.getNearCacheHits()).isZero();
            assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(50L);
            assertThat(clientStatistics.getNearCacheSize()).isEqualTo(50L);
            assertThat(clientStatistics.getNearCacheInvalidations()).isZero();

            IntStream.rangeClosed(1, 50).forEach(i -> assertThat(cache.get("key" + i)).isEqualTo("value1-" + i));

            assertThat(clientStatistics.getNearCacheHits()).isEqualTo(50L);
            assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(50L);
            assertThat(clientStatistics.getNearCacheSize()).isEqualTo(50L);
            assertThat(clientStatistics.getNearCacheInvalidations()).isZero();

            IntStream.rangeClosed(51, 100).forEach(i -> assertThat(cache.get("key" + i)).isEqualTo("value1-" + i));

            assertThat(clientStatistics.getNearCacheHits()).isEqualTo(50L);
            assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(100L);
            assertThat(clientStatistics.getNearCacheSize()).isEqualTo(100L);
            assertThat(clientStatistics.getNearCacheInvalidations()).isZero();

            IntStream.rangeClosed(51, 100).forEach(i -> assertThat(cache.get("key" + i)).isEqualTo("value1-" + i));

            assertThat(clientStatistics.getNearCacheHits()).isEqualTo(100L);
            assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(100L);
            assertThat(clientStatistics.getNearCacheSize()).isEqualTo(100L);
            assertThat(clientStatistics.getNearCacheInvalidations()).isZero();

            IntStream.rangeClosed(1, 100).forEach(i -> cache.put("key" + i, "value2-" + i));

            assertThat(clientStatistics.getNearCacheHits()).isEqualTo(100L);
            assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(100L);
            assertThat(clientStatistics.getNearCacheSize()).isZero();
            assertThat(clientStatistics.getNearCacheInvalidations()).isEqualTo(100L);

            IntStream.rangeClosed(1, 100).forEach(i -> assertThat(cache.get("key" + i)).isEqualTo("value2-" + i));

            assertThat(clientStatistics.getNearCacheHits()).isEqualTo(100L);
            assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(200L);
            assertThat(clientStatistics.getNearCacheSize()).isEqualTo(100L);
            assertThat(clientStatistics.getNearCacheInvalidations()).isEqualTo(100L);

            IntStream.rangeClosed(1, 100).forEach(i -> assertThat(cache.get("key" + i)).isEqualTo("value2-" + i));

            assertThat(clientStatistics.getNearCacheHits()).isEqualTo(200L);
            assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(200L);
            assertThat(clientStatistics.getNearCacheSize()).isEqualTo(100L);
            assertThat(clientStatistics.getNearCacheInvalidations()).isEqualTo(100L);
        }
    }

    @Test
    void enableBoundedNearCache() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServers("172.18.0.2:11222;172.18.0.3:11222;172.18.0.4:11222");
        builder.security().authentication().username("ispn-user").password("password");

        builder.remoteCache("distCache").nearCacheMode(NearCacheMode.INVALIDATED).nearCacheMaxEntries(30);

        Configuration configuration = builder.build();

        try (RemoteCacheManager cacheManager = new RemoteCacheManager(configuration)) {
            RemoteCache<String, String> cache = cacheManager.getCache("distCache");
            assertThat(cache).isInstanceOf(InvalidatedNearRemoteCache.class);

            RemoteCacheClientStatisticsMXBean clientStatistics = cache.clientStatistics();

            assertThat(clientStatistics.getNearCacheHits()).isZero();
            assertThat(clientStatistics.getNearCacheMisses()).isZero();
            assertThat(clientStatistics.getNearCacheSize()).isZero();
            assertThat(clientStatistics.getNearCacheInvalidations()).isZero();

            IntStream.rangeClosed(1, 100).forEach(i -> cache.put("key" + i, "value1-" + i));

            assertThat(clientStatistics.getNearCacheHits()).isZero();
            assertThat(clientStatistics.getNearCacheMisses()).isZero();
            assertThat(clientStatistics.getNearCacheSize()).isZero();
            assertThat(clientStatistics.getNearCacheInvalidations()).isZero();

            IntStream.rangeClosed(1, 50).forEach(i -> assertThat(cache.get("key" + i)).isEqualTo("value1-" + i));

            assertThat(clientStatistics.getNearCacheHits()).isZero();
            assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(50L);
            assertThat(clientStatistics.getNearCacheSize()).isEqualTo(30L);
            assertThat(clientStatistics.getNearCacheInvalidations()).isZero();

            IntStream.rangeClosed(1, 50).forEach(i -> assertThat(cache.get("key" + i)).isEqualTo("value1-" + i));

            assertThat(clientStatistics.getNearCacheHits()).isEqualTo(29L);
            assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(71L);
            assertThat(clientStatistics.getNearCacheSize()).isEqualTo(30L);
            assertThat(clientStatistics.getNearCacheInvalidations()).isZero();

            IntStream.rangeClosed(51, 100).forEach(i -> assertThat(cache.get("key" + i)).isEqualTo("value1-" + i));

            assertThat(clientStatistics.getNearCacheHits()).isEqualTo(29L);
            assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(121L);
            assertThat(clientStatistics.getNearCacheSize()).isEqualTo(30L);
            assertThat(clientStatistics.getNearCacheInvalidations()).isZero();

            IntStream.rangeClosed(51, 100).forEach(i -> assertThat(cache.get("key" + i)).isEqualTo("value1-" + i));

            assertThat(clientStatistics.getNearCacheHits()).isEqualTo(32L);
            assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(168L);
            assertThat(clientStatistics.getNearCacheSize()).isEqualTo(30L);
            assertThat(clientStatistics.getNearCacheInvalidations()).isZero();

            IntStream.rangeClosed(1, 100).forEach(i -> cache.put("key" + i, "value2-" + i));

            assertThat(clientStatistics.getNearCacheHits()).isEqualTo(32L);
            assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(168L);
            assertThat(clientStatistics.getNearCacheSize()).isZero();
            assertThat(clientStatistics.getNearCacheInvalidations()).isEqualTo(30L);

            IntStream.rangeClosed(1, 100).forEach(i -> assertThat(cache.get("key" + i)).isEqualTo("value2-" + i));

            assertThat(clientStatistics.getNearCacheHits()).isEqualTo(32L);
            assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(268L);
            assertThat(clientStatistics.getNearCacheSize()).isEqualTo(30L);
            assertThat(clientStatistics.getNearCacheInvalidations()).isEqualTo(30L);

            IntStream.rangeClosed(1, 100).forEach(i -> assertThat(cache.get("key" + i)).isEqualTo("value2-" + i));

            assertThat(clientStatistics.getNearCacheHits()).isGreaterThanOrEqualTo(47L).isLessThanOrEqualTo(49L);
            assertThat(clientStatistics.getNearCacheMisses()).isGreaterThanOrEqualTo(351L).isLessThanOrEqualTo(353L);
            //assertThat(clientStatistics.getNearCacheHits()).isEqualTo(49L);
            //assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(353L);
            assertThat(clientStatistics.getNearCacheSize()).isEqualTo(30L);
            assertThat(clientStatistics.getNearCacheInvalidations()).isEqualTo(30L);
        }
    }

    @Test
    void enableBoundedWithBloomFilterNearCache() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServers("172.18.0.2:11222;172.18.0.3:11222;172.18.0.4:11222");
        builder.security().authentication().username("ispn-user").password("password");
        builder.connectionPool().maxActive(1).exhaustedAction(ExhaustedAction.WAIT);

        builder.remoteCache("distCache")
                .nearCacheMode(NearCacheMode.INVALIDATED)
                .nearCacheMaxEntries(30)
                .nearCacheUseBloomFilter(true);

        Configuration configuration = builder.build();

        try (RemoteCacheManager cacheManager = new RemoteCacheManager(configuration)) {
            RemoteCache<String, String> cache = cacheManager.getCache("distCache");
            assertThat(cache).isInstanceOf(InvalidatedNearRemoteCache.class);

            RemoteCacheClientStatisticsMXBean clientStatistics = cache.clientStatistics();

            assertThat(clientStatistics.getNearCacheHits()).isZero();
            assertThat(clientStatistics.getNearCacheMisses()).isZero();
            assertThat(clientStatistics.getNearCacheSize()).isZero();
            assertThat(clientStatistics.getNearCacheInvalidations()).isZero();

            IntStream.rangeClosed(1, 100).forEach(i -> cache.put("key" + i, "value1-" + i));

            assertThat(clientStatistics.getNearCacheHits()).isZero();
            assertThat(clientStatistics.getNearCacheMisses()).isZero();
            assertThat(clientStatistics.getNearCacheSize()).isZero();
            assertThat(clientStatistics.getNearCacheInvalidations()).isZero();

            IntStream.rangeClosed(1, 50).forEach(i -> assertThat(cache.get("key" + i)).isEqualTo("value1-" + i));

            assertThat(clientStatistics.getNearCacheHits()).isZero();
            assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(50L);
            assertThat(clientStatistics.getNearCacheSize()).isEqualTo(30L);
            assertThat(clientStatistics.getNearCacheInvalidations()).isZero();

            IntStream.rangeClosed(1, 50).forEach(i -> assertThat(cache.get("key" + i)).isEqualTo("value1-" + i));

            assertThat(clientStatistics.getNearCacheHits()).isEqualTo(29L);
            assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(71L);
            assertThat(clientStatistics.getNearCacheSize()).isEqualTo(30L);
            assertThat(clientStatistics.getNearCacheInvalidations()).isZero();

            IntStream.rangeClosed(51, 100).forEach(i -> assertThat(cache.get("key" + i)).isEqualTo("value1-" + i));

            assertThat(clientStatistics.getNearCacheHits()).isEqualTo(29L);
            assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(121L);
            assertThat(clientStatistics.getNearCacheSize()).isEqualTo(30L);
            assertThat(clientStatistics.getNearCacheInvalidations()).isZero();

            IntStream.rangeClosed(51, 100).forEach(i -> assertThat(cache.get("key" + i)).isEqualTo("value1-" + i));

            assertThat(clientStatistics.getNearCacheHits()).isEqualTo(32L);
            assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(168L);
            assertThat(clientStatistics.getNearCacheSize()).isEqualTo(30L);
            assertThat(clientStatistics.getNearCacheInvalidations()).isZero();

            IntStream.rangeClosed(1, 100).forEach(i -> cache.put("key" + i, "value2-" + i));

            assertThat(clientStatistics.getNearCacheHits()).isEqualTo(32L);
            assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(168L);
            assertThat(clientStatistics.getNearCacheSize()).isZero();
            assertThat(clientStatistics.getNearCacheInvalidations()).isEqualTo(30L);

            IntStream.rangeClosed(1, 100).forEach(i -> assertThat(cache.get("key" + i)).isEqualTo("value2-" + i));

            assertThat(clientStatistics.getNearCacheHits()).isEqualTo(32L);
            assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(268L);
            assertThat(clientStatistics.getNearCacheSize()).isEqualTo(30L);
            assertThat(clientStatistics.getNearCacheInvalidations()).isEqualTo(30L);

            IntStream.rangeClosed(1, 100).forEach(i -> assertThat(cache.get("key" + i)).isEqualTo("value2-" + i));

            assertThat(clientStatistics.getNearCacheHits()).isGreaterThanOrEqualTo(47L).isLessThanOrEqualTo(49L);
            assertThat(clientStatistics.getNearCacheMisses()).isGreaterThanOrEqualTo(351L).isLessThanOrEqualTo(353L);
            //assertThat(clientStatistics.getNearCacheHits()).isEqualTo(49L);
            //assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(353L);
            assertThat(clientStatistics.getNearCacheSize()).isEqualTo(30L);
            assertThat(clientStatistics.getNearCacheInvalidations()).isEqualTo(30L);
        }
    }

    @Test
    void multipleCacheManager() {
        Supplier<RemoteCacheManager> createCacheManager = () -> {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.addServers("172.18.0.2:11222;172.18.0.3:11222;172.18.0.4:11222");
            builder.security().authentication().username("ispn-user").password("password");
            builder.connectionPool().maxActive(1).exhaustedAction(ExhaustedAction.WAIT);

            builder.remoteCache("distCache")
                    .nearCacheMode(NearCacheMode.INVALIDATED)
                    .nearCacheMaxEntries(30)
                    .nearCacheUseBloomFilter(true);

            Configuration configuration = builder.build();

            return new RemoteCacheManager(configuration);
        };

        try (RemoteCacheManager cacheManager1 = createCacheManager.get();
             RemoteCacheManager cacheManager2 = createCacheManager.get()) {
            RemoteCache<String, String> cache1 = cacheManager1.getCache("distCache");
            RemoteCacheClientStatisticsMXBean clientStatistics1 = cache1.clientStatistics();

            RemoteCache<String, String> cache2 = cacheManager2.getCache("distCache");
            RemoteCacheClientStatisticsMXBean clientStatistics2 = cache2.clientStatistics();

            cache1.put("key", "value-from-cache1");

            assertThat(clientStatistics1.getNearCacheSize()).isZero();
            assertThat(clientStatistics2.getNearCacheSize()).isZero();

            assertThat(cache1.get("key")).isEqualTo("value-from-cache1");

            assertThat(clientStatistics1.getNearCacheHits()).isZero();
            assertThat(clientStatistics1.getNearCacheMisses()).isEqualTo(1L);
            assertThat(clientStatistics1.getNearCacheSize()).isEqualTo(1L);
            assertThat(clientStatistics1.getNearCacheInvalidations()).isZero();

            assertThat(clientStatistics2.getNearCacheHits()).isZero();
            assertThat(clientStatistics2.getNearCacheMisses()).isZero();
            assertThat(clientStatistics2.getNearCacheSize()).isZero();
            assertThat(clientStatistics2.getNearCacheInvalidations()).isZero();

            assertThat(cache1.get("key")).isEqualTo("value-from-cache1");

            assertThat(clientStatistics1.getNearCacheHits()).isEqualTo(1L);
            assertThat(clientStatistics1.getNearCacheMisses()).isEqualTo(1L);
            assertThat(clientStatistics1.getNearCacheSize()).isEqualTo(1L);
            assertThat(clientStatistics1.getNearCacheInvalidations()).isZero();

            assertThat(clientStatistics2.getNearCacheHits()).isZero();
            assertThat(clientStatistics2.getNearCacheMisses()).isZero();
            assertThat(clientStatistics2.getNearCacheSize()).isZero();
            assertThat(clientStatistics2.getNearCacheInvalidations()).isZero();

            assertThat(cache2.get("key")).isEqualTo("value-from-cache1");

            assertThat(clientStatistics1.getNearCacheHits()).isEqualTo(1L);
            assertThat(clientStatistics1.getNearCacheMisses()).isEqualTo(1L);
            assertThat(clientStatistics1.getNearCacheSize()).isEqualTo(1L);
            assertThat(clientStatistics1.getNearCacheInvalidations()).isZero();

            assertThat(clientStatistics2.getNearCacheHits()).isZero();
            assertThat(clientStatistics2.getNearCacheMisses()).isEqualTo(1L);
            assertThat(clientStatistics2.getNearCacheSize()).isEqualTo(1L);
            assertThat(clientStatistics2.getNearCacheInvalidations()).isZero();

            cache2.put("key", "value-from-cache2");

            assertThat(clientStatistics1.getNearCacheHits()).isEqualTo(1L);
            assertThat(clientStatistics1.getNearCacheMisses()).isEqualTo(1L);
            assertThat(clientStatistics1.getNearCacheSize()).isZero();
            assertThat(clientStatistics1.getNearCacheInvalidations()).isEqualTo(1L);

            assertThat(clientStatistics2.getNearCacheHits()).isZero();
            assertThat(clientStatistics2.getNearCacheMisses()).isEqualTo(1L);
            assertThat(clientStatistics2.getNearCacheSize()).isZero();
            assertThat(clientStatistics2.getNearCacheInvalidations()).isEqualTo(1L);
        }
    }
}
