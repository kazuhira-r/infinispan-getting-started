package org.littlewings.infinspan.remote.iouring;

import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.RemoteCacheManagerAdmin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HotRodClientIoUringTest {
    static {
        System.setProperty("infinispan.server.channel.epoll", "false");
    }

    static String createUri(String userName, String password) {
        return String.format(
                "hotrod://%s:%s@172.18.0.2:11222,172.18.0.3:11222,172.18.0.4:11222",
                userName,
                password
        );
    }

    @BeforeAll
    static void setUpAll() {
        String uri = createUri("ispn-admin", "password");

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

    <K, V> void withCache(String cacheName, Consumer<RemoteCache<K, V>> func) {
        String uri = createUri("ispn-user", "password");

        try (RemoteCacheManager manager = new RemoteCacheManager(uri)) {
            RemoteCache<K, V> cache = manager.getCache(cacheName);

            func.accept(cache);
        }
    }

    @Test
    void transportIoUring() {
        this.<String, String>withCache("distCache", cache -> {
            IntStream
                    .rangeClosed(1, 100)
                    .forEach(i -> cache.put("key" + i, "value" + i));

            IntStream
                    .rangeClosed(1, 100)
                    .forEach(i -> assertThat(cache.get("key" + i)).isEqualTo("value" + i));
        });
    }
}
