package org.littlewings.infinispan.configuration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.ClusteringConfiguration;
import org.infinispan.configuration.cache.ExpirationConfiguration;
import org.infinispan.configuration.cache.MemoryConfiguration;
import org.infinispan.configuration.cache.StorageType;
import org.infinispan.configuration.cache.TransactionConfiguration;
import org.infinispan.eviction.EvictionType;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ConfigurationTemplateTest {
    <K, V> void withCache(String configurationFileName, int numOfInstances, String cacheName, Consumer<Cache<K, V>> func) {
        List<EmbeddedCacheManager> managers =
                IntStream
                        .rangeClosed(1, numOfInstances)
                        .mapToObj(i -> {
                            try {
                                return new DefaultCacheManager(configurationFileName);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        })
                        .collect(Collectors.toList());

        try {
            managers.forEach(manager -> manager.getCache(cacheName));

            Cache<K, V> cache = managers.get(0).getCache(cacheName);
            func.accept(cache);
        } finally {
            managers.forEach(EmbeddedCacheManager::stop);
        }
    }

    @Test
    public void simpleDistributedCache() {
        this.<String, String>withCache("infinispan-simple.xml", 3, "simpleDistributedCache", cache -> {
            ClusteringConfiguration clusteringConfiguration = cache.getCacheConfiguration().clustering();
            assertThat(clusteringConfiguration.cacheMode().isDistributed()).isTrue();

            ExpirationConfiguration expirationConfiguration = cache.getCacheConfiguration().expiration();
            assertThat(expirationConfiguration.maxIdle()).isEqualTo(5L);
            assertThat(expirationConfiguration.lifespan()).isEqualTo(10L);
        });
    }

    @Test
    public void useExpireCacheTemplate() {
        this.<String, String>withCache("infinispan-use-template1.xml", 3, "expirationCache", cache -> {
            ClusteringConfiguration clusteringConfiguration = cache.getCacheConfiguration().clustering();
            assertThat(clusteringConfiguration.cacheMode().isDistributed()).isTrue();

            ExpirationConfiguration expirationConfiguration = cache.getCacheConfiguration().expiration();
            assertThat(expirationConfiguration.maxIdle()).isEqualTo(5L);
            assertThat(expirationConfiguration.lifespan()).isEqualTo(10L);
        });

        this.<String, String>withCache("infinispan-use-template1.xml", 3, "expirationExtendedCache", cache -> {
            ClusteringConfiguration clusteringConfiguration = cache.getCacheConfiguration().clustering();
            assertThat(clusteringConfiguration.cacheMode().isDistributed()).isTrue();

            ExpirationConfiguration expirationConfiguration = cache.getCacheConfiguration().expiration();
            assertThat(expirationConfiguration.maxIdle()).isEqualTo(5L);
            assertThat(expirationConfiguration.lifespan()).isEqualTo(15L);

            TransactionConfiguration transactionConfiguration = cache.getCacheConfiguration().transaction();
            assertThat(transactionConfiguration.transactionMode().isTransactional()).isTrue();
        });
    }

    @Test
    public void useBadTemplate1() {
        assertThatThrownBy(() -> new DefaultCacheManager("infinispan-bad-template1.xml"))
                .hasMessage("ISPN000374: No such template 'expirationCacheTemplate' when declaring 'expirationCache'");
    }

    @Test
    public void useBadTemplate2() {
        this.<String, String>withCache("infinispan-bad-template2.xml", 3, "expirationCache", cache -> {
            ClusteringConfiguration clusteringConfiguration = cache.getCacheConfiguration().clustering();
            assertThat(clusteringConfiguration.cacheMode().isReplicated()).isTrue();

            ExpirationConfiguration expirationConfiguration = cache.getCacheConfiguration().expiration();
            assertThat(expirationConfiguration.maxIdle()).isEqualTo(5L);
            assertThat(expirationConfiguration.lifespan()).isEqualTo(10L);
        });
    }

    @Test
    public void useOffHeapCacheTemplate() {
        this.<String, String>withCache("infinispan-use-template2.xml", 3, "offHeapCache", cache -> {
            ClusteringConfiguration clusteringConfiguration = cache.getCacheConfiguration().clustering();
            assertThat(clusteringConfiguration.cacheMode().isDistributed()).isTrue();

            MemoryConfiguration memoryConfiguration = cache.getCacheConfiguration().memory();
            assertThat(memoryConfiguration.storageType()).isEqualTo(StorageType.OFF_HEAP);
            assertThat(memoryConfiguration.evictionType()).isEqualTo(EvictionType.MEMORY);
            assertThat(memoryConfiguration.size()).isEqualTo(896000L);
        });

        this.<String, String>withCache("infinispan-use-template2.xml", 3, "offHeapWithExpirationCache", cache -> {
            ClusteringConfiguration clusteringConfiguration = cache.getCacheConfiguration().clustering();
            assertThat(clusteringConfiguration.cacheMode().isDistributed()).isTrue();

            MemoryConfiguration memoryConfiguration = cache.getCacheConfiguration().memory();
            assertThat(memoryConfiguration.storageType()).isEqualTo(StorageType.OFF_HEAP);
            assertThat(memoryConfiguration.evictionType()).isEqualTo(EvictionType.MEMORY);
            assertThat(memoryConfiguration.size()).isEqualTo(1792000L);

            ExpirationConfiguration expirationConfiguration = cache.getCacheConfiguration().expiration();
            assertThat(expirationConfiguration.maxIdle()).isEqualTo(5L);
            assertThat(expirationConfiguration.lifespan()).isEqualTo(15L);
            assertThat(expirationConfiguration.wakeUpInterval()).isEqualTo(120000L);

            TransactionConfiguration transactionConfiguration = cache.getCacheConfiguration().transaction();
            assertThat(transactionConfiguration.transactionMode().isTransactional()).isTrue();
        });
    }

    @Test
    public void useGlobTemplate() {
        this.<String, String>withCache("infinispan-use-glob.xml", 3, "expirationCache1", cache -> {
            ClusteringConfiguration clusteringConfiguration = cache.getCacheConfiguration().clustering();
            assertThat(clusteringConfiguration.cacheMode().isDistributed()).isTrue();

            ExpirationConfiguration expirationConfiguration = cache.getCacheConfiguration().expiration();
            assertThat(expirationConfiguration.maxIdle()).isEqualTo(5L);
            assertThat(expirationConfiguration.lifespan()).isEqualTo(10L);
        });

        this.<String, String>withCache("infinispan-use-glob.xml", 3, "expirationCache2", cache -> {
            ClusteringConfiguration clusteringConfiguration = cache.getCacheConfiguration().clustering();
            assertThat(clusteringConfiguration.cacheMode().isDistributed()).isTrue();

            ExpirationConfiguration expirationConfiguration = cache.getCacheConfiguration().expiration();
            assertThat(expirationConfiguration.maxIdle()).isEqualTo(5L);
            assertThat(expirationConfiguration.lifespan()).isEqualTo(10L);
        });

        this.<String, String>withCache("infinispan-use-glob.xml", 3, "myExpirationCache", cache -> {
            ClusteringConfiguration clusteringConfiguration = cache.getCacheConfiguration().clustering();
            assertThat(clusteringConfiguration.cacheMode().isDistributed()).isTrue();

            ExpirationConfiguration expirationConfiguration = cache.getCacheConfiguration().expiration();
            assertThat(expirationConfiguration.maxIdle()).isEqualTo(5L);
            assertThat(expirationConfiguration.lifespan()).isEqualTo(10L);
        });


        assertThatThrownBy(() -> {
            EmbeddedCacheManager manager = new DefaultCacheManager("infinispan-use-glob.xml");

            try {
                manager.getCache("simpleCache");
            } finally {
                manager.stop();
            }
        })
                .hasMessage("ISPN000436: Cache 'simpleCache' has been requested, but no cache configuration exists with that name and no default cache has been set for this container");
    }
}
