package org.littlewings.infinispan.remote.nearcache;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.RemoteCacheManagerAdmin;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.ExhaustedAction;
import org.infinispan.client.hotrod.configuration.NearCacheMode;
import org.infinispan.client.hotrod.jmx.RemoteCacheClientStatisticsMXBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class NearCacheWithExpirationTest {
    @BeforeEach
    void setUp() {
        String uri = String.format(
                "hotrod://%s:%s@172.18.0.2:11222,172.18.0.3:11222,172.18.0.4:11222",
                "ispn-admin",
                "password"
        );

        try (RemoteCacheManager manager = new RemoteCacheManager(uri)) {
            RemoteCacheManagerAdmin admin = manager.administration();

            admin.removeCache("distCacheWithIdleExpiration");

            org.infinispan.configuration.cache.Configuration configurationWithIdleExpiration =
                    new org.infinispan.configuration.cache.ConfigurationBuilder()
                            .clustering()
                            .cacheMode(org.infinispan.configuration.cache.CacheMode.DIST_SYNC)
                            .encoding().key().mediaType("application/x-protostream")
                            .encoding().value().mediaType("application/x-protostream")
                            .expiration().maxIdle(3L, TimeUnit.SECONDS).wakeUpInterval(1L, TimeUnit.SECONDS)
                            .build();

            admin.getOrCreateCache("distCacheWithIdleExpiration", configurationWithIdleExpiration);

            admin.removeCache("distCacheWithLifespanExpiration");

            org.infinispan.configuration.cache.Configuration configurationWithLifespanExpiration =
                    new org.infinispan.configuration.cache.ConfigurationBuilder()
                            .clustering()
                            .cacheMode(org.infinispan.configuration.cache.CacheMode.DIST_SYNC)
                            .encoding().key().mediaType("application/x-protostream")
                            .encoding().value().mediaType("application/x-protostream")
                            .expiration().lifespan(3L, TimeUnit.SECONDS).wakeUpInterval(1L, TimeUnit.SECONDS)
                            .build();

            admin.getOrCreateCache("distCacheWithLifespanExpiration", configurationWithLifespanExpiration);
        }
    }

    @Test
    void nearCacheWithMaxIdleExpiration() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServers("172.18.0.2:11222;172.18.0.3:11222;172.18.0.4:11222");
        builder.security().authentication().username("ispn-user").password("password");
        builder.connectionPool().maxActive(1).exhaustedAction(ExhaustedAction.WAIT);

        builder.remoteCache("distCacheWithIdleExpiration")
                .nearCacheMode(NearCacheMode.INVALIDATED)
                .nearCacheMaxEntries(30)
                .nearCacheUseBloomFilter(true);

        Configuration configuration = builder.build();

        try (RemoteCacheManager cacheManager = new RemoteCacheManager(configuration)) {
            RemoteCache<String, String> cache = cacheManager.getCache("distCacheWithIdleExpiration");
            RemoteCacheClientStatisticsMXBean clientStatistics = cache.clientStatistics();

            cache.put("key", "value");
            assertThat(cache.get("key")).isEqualTo("value");

            assertThat(clientStatistics.getNearCacheHits()).isZero();
            assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(1L);
            assertThat(clientStatistics.getNearCacheSize()).isEqualTo(1L);
            assertThat(clientStatistics.getNearCacheInvalidations()).isZero();

            IntStream.rangeClosed(1, 3).forEach(i -> {
                try {
                    TimeUnit.SECONDS.sleep(1L);
                } catch (InterruptedException e) {
                    // ignore
                }

                assertThat(cache.get("key")).isEqualTo("value");

                assertThat(clientStatistics.getNearCacheHits()).isEqualTo(i);
                assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(1L);
                assertThat(clientStatistics.getNearCacheSize()).isEqualTo(1L);
                assertThat(clientStatistics.getNearCacheInvalidations()).isZero();
            });

            try {
                   TimeUnit.SECONDS.sleep(1L);
               } catch (InterruptedException e) {
                   // ignore
            }

            assertThat(cache.get("key")).isNull();

            assertThat(clientStatistics.getNearCacheHits()).isEqualTo(3L);
            assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(2L);
            assertThat(clientStatistics.getNearCacheSize()).isZero();
            assertThat(clientStatistics.getNearCacheInvalidations()).isEqualTo(1L);
        }
    }

    @Test
    void maxIdleExpiration() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServers("172.18.0.2:11222;172.18.0.3:11222;172.18.0.4:11222");
        builder.security().authentication().username("ispn-user").password("password");

        /*
        builder.remoteCache("distCacheWithIdleExpiration")
                .nearCacheMode(NearCacheMode.INVALIDATED)
                .nearCacheMaxEntries(30)
                .nearCacheUseBloomFilter(true);
        */

        Configuration configuration = builder.build();

        try (RemoteCacheManager cacheManager = new RemoteCacheManager(configuration)) {
            RemoteCache<String, String> cache = cacheManager.getCache("distCacheWithIdleExpiration");

            cache.put("key", "value");
            assertThat(cache.get("key")).isEqualTo("value");

            IntStream.rangeClosed(1, 3).forEach(i -> {
                try {
                    TimeUnit.SECONDS.sleep(1L);
                } catch (InterruptedException e) {
                    // ignore
                }

                assertThat(cache.get("key")).isEqualTo("value");
            });

            try {
                   TimeUnit.SECONDS.sleep(1L);
               } catch (InterruptedException e) {
                   // ignore
            }

            assertThat(cache.get("key")).isEqualTo("value");

            try {
                   TimeUnit.SECONDS.sleep(3L);
               } catch (InterruptedException e) {
                   // ignore
            }

            assertThat(cache.get("key")).isNull();
        }
    }

    @Test
    void nearCacheWithMaxIdleExpirationPerEntry() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServers("172.18.0.2:11222;172.18.0.3:11222;172.18.0.4:11222");
        builder.security().authentication().username("ispn-user").password("password");
        builder.connectionPool().maxActive(1).exhaustedAction(ExhaustedAction.WAIT);

        builder.remoteCache("distCacheWithIdleExpiration")
                .nearCacheMode(NearCacheMode.INVALIDATED)
                .nearCacheMaxEntries(30)
                .nearCacheUseBloomFilter(true);

        Configuration configuration = builder.build();

        try (RemoteCacheManager cacheManager = new RemoteCacheManager(configuration)) {
            RemoteCache<String, String> cache = cacheManager.getCache("distCacheWithIdleExpiration");
            RemoteCacheClientStatisticsMXBean clientStatistics = cache.clientStatistics();

            cache.put("key", "value", -1L, TimeUnit.SECONDS, 5L, TimeUnit.SECONDS);

            assertThat(cache.get("key")).isEqualTo("value");

            assertThat(clientStatistics.getNearCacheHits()).isZero();
            assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(1L);
            assertThat(clientStatistics.getNearCacheSize()).isEqualTo(1L);
            assertThat(clientStatistics.getNearCacheInvalidations()).isZero();

            IntStream.rangeClosed(1, 5).forEach(i -> {
                try {
                    TimeUnit.SECONDS.sleep(1L);
                } catch (InterruptedException e) {
                    // ignore
                }

                assertThat(cache.get("key")).isEqualTo("value");

                assertThat(clientStatistics.getNearCacheHits()).isEqualTo(i);
                assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(1L);
                assertThat(clientStatistics.getNearCacheSize()).isEqualTo(1L);
                assertThat(clientStatistics.getNearCacheInvalidations()).isZero();
            });

            try {
                TimeUnit.SECONDS.sleep(5L);
            } catch (InterruptedException e) {
                // ignore
            }

            assertThat(cache.get("key")).isNull();

            assertThat(clientStatistics.getNearCacheHits()).isEqualTo(5L);
            assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(2L);
            assertThat(clientStatistics.getNearCacheSize()).isZero();
            assertThat(clientStatistics.getNearCacheInvalidations()).isEqualTo(1L);
        }
    }

    @Test
    void nearCacheWithLifespanExpiration() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServers("172.18.0.2:11222;172.18.0.3:11222;172.18.0.4:11222");
        builder.security().authentication().username("ispn-user").password("password");
        builder.connectionPool().maxActive(1).exhaustedAction(ExhaustedAction.WAIT);

        builder.remoteCache("distCacheWithLifespanExpiration")
                .nearCacheMode(NearCacheMode.INVALIDATED)
                .nearCacheMaxEntries(30)
                .nearCacheUseBloomFilter(true);

        Configuration configuration = builder.build();

        try (RemoteCacheManager cacheManager = new RemoteCacheManager(configuration)) {
            RemoteCache<String, String> cache = cacheManager.getCache("distCacheWithLifespanExpiration");
            RemoteCacheClientStatisticsMXBean clientStatistics = cache.clientStatistics();

            cache.put("key", "value");
            assertThat(cache.get("key")).isEqualTo("value");

            assertThat(clientStatistics.getNearCacheHits()).isZero();
            assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(1L);
            assertThat(clientStatistics.getNearCacheSize()).isEqualTo(1L);
            assertThat(clientStatistics.getNearCacheInvalidations()).isZero();

            IntStream.rangeClosed(1, 3).forEach(i -> {
                try {
                    TimeUnit.SECONDS.sleep(1L);
                } catch (InterruptedException e) {
                    // ignore
                }

                assertThat(cache.get("key")).isEqualTo("value");

                assertThat(clientStatistics.getNearCacheHits()).isEqualTo(i);
                assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(1L);
                assertThat(clientStatistics.getNearCacheSize()).isEqualTo(1L);
                assertThat(clientStatistics.getNearCacheInvalidations()).isZero();
            });

            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (InterruptedException e) {
                // ignore
            }

            assertThat(cache.get("key")).isNull();

            assertThat(clientStatistics.getNearCacheHits()).isEqualTo(3L);
            assertThat(clientStatistics.getNearCacheMisses()).isEqualTo(2L);
            assertThat(clientStatistics.getNearCacheSize()).isZero();
            assertThat(clientStatistics.getNearCacheInvalidations()).isEqualTo(1L);
        }
    }
}
