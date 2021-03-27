package org.littlewings.infinispan.client.connect;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConnectServerTest {
    @Test
    public void usingMethodBasedConfiguration() {
        Configuration configuration =
                new ConfigurationBuilder()
                        .addServers("172.17.0.2:11222;172.17.0.3:11222;172.17.0.4:11222")
                        .connectionPool()
                        .maxActive(10)
                        .maxWait(10000)
                        .connectionTimeout(30000)
                        .socketTimeout(30000)
                        .security()
                        .authentication()
                        .saslMechanism("SCRAM-SHA-256")
                        .username("ispn-user")
                        .password("ispn-password".toCharArray())
                        .build();

        RemoteCacheManager manager = new RemoteCacheManager(configuration);

        Configuration c = manager.getConfiguration();

        assertThat(c.connectionPool().maxActive()).isEqualTo(10);
        assertThat(c.connectionPool().maxWait()).isEqualTo(10000);
        assertThat(c.connectionTimeout()).isEqualTo(30000);
        assertThat(c.socketTimeout()).isEqualTo(30000);
        assertThat(c.security().authentication().saslMechanism()).isEqualTo("SCRAM-SHA-256");

        try {
            RemoteCache<String, String> cache = manager.getCache("myCache");

            CompletableFuture<String> putAndGet =
                    cache
                            .putAsync("key1", "value1")
                            .thenCompose(v -> cache.getAsync("key1"));

            assertThat(putAndGet)
                    .succeedsWithin(Duration.ofSeconds(10))
                    .isEqualTo("value1");

            CompletableFuture<String> removeAndGet =
                    cache.removeAsync("key1")
                            .thenCompose(v -> cache.getAsync("key1"));

            assertThat(removeAndGet)
                    .succeedsWithin(Duration.ofSeconds(10))
                    .isNull();
        } finally {
            manager.stop();
        }
    }

    @Test
    public void usingUri() {
        Configuration configuration =
                new ConfigurationBuilder()
                        .uri("hotrod://ispn-user:ispn-password@172.17.0.2:11222,172.17.0.3:11222,172.17.0.4:11222?sasl_mechanism=SCRAM-SHA-256&connection_pool.max_active=10&connect_timeout=30000&socket_timeout=30000")
                        .connectionPool()
                        .maxWait(10000)
                        .build();

        RemoteCacheManager manager = new RemoteCacheManager(configuration);

        Configuration c = manager.getConfiguration();

        assertThat(c.connectionPool().maxActive()).isEqualTo(10);
        assertThat(c.connectionPool().maxWait()).isEqualTo(10000);
        assertThat(c.connectionTimeout()).isEqualTo(30000);
        assertThat(c.socketTimeout()).isEqualTo(30000);
        assertThat(c.security().authentication().saslMechanism()).isEqualTo("SCRAM-SHA-256");

        try {
            RemoteCache<String, String> cache = manager.getCache("myCache");

            CompletableFuture<String> putAndGet =
                    cache
                            .putAsync("key1", "value1")
                            .thenCompose(v -> cache.getAsync("key1"));

            assertThat(putAndGet)
                    .succeedsWithin(Duration.ofSeconds(10))
                    .isEqualTo("value1");

            CompletableFuture<String> removeAndGet =
                    cache.removeAsync("key1")
                            .thenCompose(v -> cache.getAsync("key1"));

            assertThat(removeAndGet)
                    .succeedsWithin(Duration.ofSeconds(10))
                    .isNull();
        } finally {
            manager.stop();
        }
    }
}
