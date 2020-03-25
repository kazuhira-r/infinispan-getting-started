package org.littlewings.infinispan.persistence;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.PersistenceConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleMapStoreTest {
    <K, V> void withCache(String cacheName, int numInstances, Consumer<Cache<K, V>> func) {
        List<EmbeddedCacheManager> managers =
                IntStream
                        .rangeClosed(1, numInstances)
                        .mapToObj(i -> {
                                    try {
                                        return new DefaultCacheManager("infinispan.xml");
                                    } catch (IOException e) {
                                        throw new UncheckedIOException(e);
                                    }
                                }
                        )
                        .collect(Collectors.toList());

        try {
            managers.forEach(m -> m.getCache(cacheName));

            Cache<K, V> cache = managers.get(0).getCache(cacheName);
            func.accept(cache);
        } finally {
            managers.forEach(EmbeddedCacheManager::stop);
        }
    }

    @Test
    public void withDeclarativeSimpleCacheStore() {
        this.<String, String>withCache("declarativeSimpleStoreCache", 3, cache -> {
            System.out.println("============================== start ==============================");

            System.out.println("[Data Put] start");
            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, "value" + i));
            System.out.println("[Data Put] end");

            System.out.println("[Data Get] start");
            assertThat(cache.get("key1")).isEqualTo("value1");
            System.out.println("[Data Get] end");

            assertThat(cache.containsKey("key2")).isTrue();

            System.out.println("[Data Remove] start");
            cache.remove("key3");
            System.out.println("[Data Remove] end");

            System.out.println("[Data Clear] start");
            cache.clear();
            System.out.println("[Data Clear] end");

            assertThat(cache.containsKey("key2")).isTrue();

            System.out.println("[Data Get] start");
            IntStream.rangeClosed(1, 10).forEach(i -> cache.get("key" + i));
            System.out.println("[Data Get] end");

            System.out.println("============================== end ==============================");
        });
    }

    @Test
    public void withProgrammaticallyDeclarativeSimpleCacheStore() throws IOException {
        try (EmbeddedCacheManager manager = new DefaultCacheManager("infinispan.xml")) {

            PersistenceConfigurationBuilder persistenceConfigurationBuilder =
                    new ConfigurationBuilder()
                            .clustering()
                            .cacheMode(CacheMode.DIST_SYNC)
                            .persistence();

            Configuration configuration =
                    persistenceConfigurationBuilder
                            .addStore(new SimpleMapCacheStoreConfigurationBuilder(persistenceConfigurationBuilder))
                            .addProperty("storeName", "programmaticallyStore")
                            .build();

            manager.defineConfiguration("programmaticallySimpleStoreCache", configuration);

            Cache<String, String> cache = manager.getCache("programmaticallySimpleStoreCache");

            System.out.println("============================== start ==============================");

            System.out.println("[Data Put] start");
            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, "value" + i));
            System.out.println("[Data Put] end");

            System.out.println("[Data Get] start");
            assertThat(cache.get("key1")).isEqualTo("value1");
            System.out.println("[Data Get] end");

            assertThat(cache.containsKey("key2")).isTrue();

            System.out.println("[Data Remove] start");
            cache.remove("key3");
            System.out.println("[Data Remove] end");

            System.out.println("[Data Clear] start");
            cache.clear();
            System.out.println("[Data Clear] end");

            assertThat(cache.containsKey("key2")).isTrue();

            System.out.println("[Data Get] start");
            IntStream.rangeClosed(1, 10).forEach(i -> cache.get("key" + i));
            System.out.println("[Data Get] end");

            System.out.println("============================== end ==============================");
        }
    }
}
