package org.littlewings.infinispan.authentication.token;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.exceptions.HotRodClientException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TokenAuthnAuthzTest {
    @Test
    public void authnAndAuthz() {
        String readWriteUserToken = "[read-writeロールを持つユーザーのアクセストークン]";

        try (RemoteCacheManager cacheManager = new RemoteCacheManager(
                new ConfigurationBuilder()
                        .addServers("172.17.0.3:11222")
                        .security()
                        .authentication()
                        .saslMechanism("OAUTHBEARER")
                        .token(readWriteUserToken)
                        .build())) {
            RemoteCache<String, String> cache = cacheManager.getCache("secureCache");

            cache.put("key1", "value1");
            assertThat(cache.get("key1")).isEqualTo("value1");
        }

        String readOnlyUserToken = "[read-onlyロールを持つユーザーのアクセストークン]";

        try (RemoteCacheManager cacheManager = new RemoteCacheManager(
                new ConfigurationBuilder()
                        .addServers("172.17.0.3:11222")
                        .security()
                        .authentication()
                        .saslMechanism("OAUTHBEARER")
                        .token(readOnlyUserToken)
                        .build())) {
            RemoteCache<String, String> cache = cacheManager.getCache("secureCache");

            assertThat(cache.get("key1")).isEqualTo("value1");

            assertThatThrownBy(() -> cache.put("key2", "value2"))
                    .isInstanceOf(HotRodClientException.class)
                    .hasMessageContainingAll("Unauthorized access", "lacks 'WRITE' permission");
        }
    }

    @Test
    public void noAuth() {
        try (RemoteCacheManager cacheManager = new RemoteCacheManager(
                new ConfigurationBuilder()
                        .addServers("172.17.0.3:11222")
                        .build())) {
            RemoteCache<String, String> cache = cacheManager.getCache("secureCache");

            assertThatThrownBy(() -> cache.put("key1", "value1"))
                    .isInstanceOf(HotRodClientException.class)
                    .hasMessageContaining("Unauthorized 'PUT' operation");
        }
    }
}
