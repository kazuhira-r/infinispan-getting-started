package org.littlewings.infinispan.authentication;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AuthnAuthzTest {
    @Disabled
    @Test
    public void plainCache() {
        Configuration configuration =
                new ConfigurationBuilder()
                        .addServers("172.17.0.2:11222")
                        .build();

        RemoteCacheManager manager = new RemoteCacheManager(configuration);

        try {
            RemoteCache<String, String> cache = manager.getCache("plainCache");

            cache.put("key1", "value1");
            assertThat(cache.get("key1")).isEqualTo("value1");
            assertThat(cache.size()).isEqualTo(1L);

            cache.clear();
            assertThat(cache.size()).isEqualTo(0L);
        } finally {
            manager.stop();
        }
    }

    @Test
    public void authentication() {
        RemoteCacheManager manager1 = new RemoteCacheManager(
                new ConfigurationBuilder()
                        .addServers("172.17.0.2:11222")
                        .security()
                        .authentication()
                        .saslMechanism("PLAIN")
                        .username("admin")
                        .password("password")
                        .build());

        try {
            assertThat(manager1.getCache("securedCache")).isNotNull();
        } finally {
            manager1.stop();
        }

        //////////////////////

        RemoteCacheManager manager2 = new RemoteCacheManager(
                new ConfigurationBuilder()
                        .addServers("172.17.0.2:11222")
                        .security()
                        .authentication()
                        .saslMechanism("PLAIN")
                        .username("unknown")
                        .password("bad-password")
                        .build());

        try {
            assertThatThrownBy(() -> manager2.getCache("securedCache"))
                    .hasMessage("org.infinispan.client.hotrod.exceptions.HotRodClientException:Request for messageId=80 returned server error (status=0x84): javax.security.sasl.SaslException: ELY05013: Authentication mechanism password not verified");
        } finally {
            manager2.stop();
        }
    }

    @Test
    public void readWriteOnlyUser() {
        RemoteCacheManager manager = new RemoteCacheManager(
                new ConfigurationBuilder()
                        .addServers("172.17.0.2:11222")
                        .security()
                        .authentication()
                        .saslMechanism("PLAIN")
                        .username("read-only-user")
                        .password("password")
                        .build());

        try {
            RemoteCache<String, String> cache = manager.getCache("securedCache");

            assertThatThrownBy(() -> cache.put("key1", "value1"))
                    .hasMessage("java.lang.SecurityException: ISPN000287: Unauthorized access: subject 'Subject with principal(s): [read-only-user, RolePrincipal{name='reader'}, InetAddressPrincipal [address=172.17.0.1/172.17.0.1]]' lacks 'WRITE' permission");
        } finally {
            manager.stop();
        }

        //////////////////////

        manager = new RemoteCacheManager(
                new ConfigurationBuilder()
                        .addServers("172.17.0.2:11222")
                        .security()
                        .authentication()
                        .saslMechanism("PLAIN")
                        .username("write-only-user")
                        .password("password")
                        .build());

        try {
            RemoteCache<String, String> cache = manager.getCache("securedCache");

            cache.put("key1", "value1");
            assertThatThrownBy(() -> cache.get("key1"))
                    .hasMessage("java.lang.SecurityException: ISPN000287: Unauthorized access: subject 'Subject with principal(s): [write-only-user, RolePrincipal{name='writer'}, InetAddressPrincipal [address=172.17.0.1/172.17.0.1]]' lacks 'READ' permission");
        } finally {
            manager.stop();
        }

        //////////////////////

        manager = new RemoteCacheManager(
                new ConfigurationBuilder()
                        .addServers("172.17.0.2:11222")
                        .security()
                        .authentication()
                        .saslMechanism("PLAIN")
                        .username("read-only-user")
                        .password("password")
                        .build());

        try {
            RemoteCache<String, String> cache = manager.getCache("securedCache");

            assertThat(cache.get("key1")).isEqualTo("value1");
        } finally {
            manager.stop();
        }

        //////////////////////

        manager = new RemoteCacheManager(
                new ConfigurationBuilder()
                        .addServers("172.17.0.2:11222")
                        .security()
                        .authentication()
                        .saslMechanism("PLAIN")
                        .username("admin")
                        .password("password")
                        .build());

        try {
            RemoteCache<String, String> cache = manager.getCache("securedCache");

            cache.clear();
        } finally {
            manager.stop();
        }
    }

    @Test
    public void readWriteRole1() {
        RemoteCacheManager manager = new RemoteCacheManager(
                new ConfigurationBuilder()
                        .addServers("172.17.0.2:11222")
                        .security()
                        .authentication()
                        .saslMechanism("PLAIN")
                        .username("read-write-user-simple")
                        .password("password")
                        .build());

        try {
            RemoteCache<String, String> cache = manager.getCache("securedCache");

            cache.put("key1", "value1");
            assertThat(cache.get("key1")).isEqualTo("value1");
        } finally {
            manager.stop();
        }

        //////////////////////

        manager = new RemoteCacheManager(
                new ConfigurationBuilder()
                        .addServers("172.17.0.2:11222")
                        .security()
                        .authentication()
                        .saslMechanism("PLAIN")
                        .username("admin")
                        .password("password")
                        .build());

        try {
            RemoteCache<String, String> cache = manager.getCache("securedCache");

            cache.clear();
        } finally {
            manager.stop();
        }
    }

    @Test
    public void readWriteRole2() {
        RemoteCacheManager manager = new RemoteCacheManager(
                new ConfigurationBuilder()
                        .addServers("172.17.0.2:11222")
                        .security()
                        .authentication()
                        .saslMechanism("PLAIN")
                        .username("read-write-user-multi")
                        .password("password")
                        .build());

        try {
            RemoteCache<String, String> cache = manager.getCache("securedCache");

            cache.put("key1", "value1");
            assertThat(cache.get("key1")).isEqualTo("value1");
        } finally {
            manager.stop();
        }

        //////////////////////

        manager = new RemoteCacheManager(
                new ConfigurationBuilder()
                        .addServers("172.17.0.2:11222")
                        .security()
                        .authentication()
                        .saslMechanism("PLAIN")
                        .username("admin")
                        .password("password")
                        .build());

        try {
            RemoteCache<String, String> cache = manager.getCache("securedCache");

            cache.clear();
        } finally {
            manager.stop();
        }
    }
}
