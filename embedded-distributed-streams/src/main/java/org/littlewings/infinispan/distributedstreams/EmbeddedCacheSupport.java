package org.littlewings.infinispan.distributedstreams;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

public abstract class EmbeddedCacheSupport {
    protected <K, V> void withCache(String cacheName, Consumer<Cache<K, V>> fun) {
        withCache(cacheName, 1, fun);
    }

    protected <K, V> void withCache(String cacheName, int numInstances, Consumer<Cache<K, V>> fun) {
        List<EmbeddedCacheManager> managers =
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

        managers.forEach(m -> m.<K, V>getCache(cacheName));

        try {
            fun.accept(managers.get(0).<K, V>getCache(cacheName));
        } finally {
            managers.forEach(m -> m.getCache(cacheName).stop());
            managers.forEach(m -> m.stop());
        }
    }
}
