package org.littlewings.remote.admin;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.RemoteCacheManagerAdmin;
import org.infinispan.client.hotrod.exceptions.HotRodClientException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CreateCacheTest {
    @BeforeAll
    public static void setupAll() {
        String uri = createUri("admin-user", "password");

        try (RemoteCacheManager manager = new RemoteCacheManager(uri)) {
            RemoteCacheManagerAdmin admin = manager.administration();

            Stream.of("distCache", "replCache").forEach(admin::removeCache);
        }
    }

    private static String createUri(String userName, String password) {
        return String.format("hotrod://%s:%s@172.17.0.2:11222,172.17.0.3:11222,172.17.0.4:11222", userName, password);
    }

    @Test
    public void createCacheByAdminUser() {
        String uri = createUri("admin-user", "password");

        try (RemoteCacheManager manager = new RemoteCacheManager(uri)) {
            RemoteCacheManagerAdmin admin = manager.administration();

            org.infinispan.configuration.cache.Configuration cacheConfiguration =
                    new org.infinispan.configuration.cache.ConfigurationBuilder()
                            .clustering()
                            .cacheMode(org.infinispan.configuration.cache.CacheMode.DIST_SYNC)
                            .encoding().key().mediaType("application/x-protostream")
                            .encoding().value().mediaType("application/x-protostream")
                            .build();

            RemoteCache<String, String> cache = admin.createCache("distCache", cacheConfiguration);

            IntStream.rangeClosed(1, 100).forEach(i -> cache.put("key" + i, "value" + i));
            assertThat(cache.size()).isEqualTo(100);

            admin.removeCache("distCache");
        }
    }

    @Test
    public void createCacheByApplicationUser() {
        String uri = createUri("app-user", "password");

        try (RemoteCacheManager manager = new RemoteCacheManager(uri)) {
            RemoteCacheManagerAdmin admin = manager.administration();

            org.infinispan.configuration.cache.Configuration cacheConfiguration =
                    new org.infinispan.configuration.cache.ConfigurationBuilder()
                            .clustering()
                            .cacheMode(org.infinispan.configuration.cache.CacheMode.DIST_SYNC)
                            .encoding().key().mediaType("application/x-protostream")
                            .encoding().value().mediaType("application/x-protostream")
                            .build();

            assertThatThrownBy(() -> admin.createCache("distCache", cacheConfiguration))
                    .hasMessageContaining("org.infinispan.commons.CacheException: java.lang.SecurityException: ISPN000287: Unauthorized access: subject 'Subject with principal(s): [app-user, RolePrincipal{name='application'},")
                    .hasMessageContaining("' lacks 'ADMIN' permission")
                    .isInstanceOf(HotRodClientException.class);
        }
    }

    @Test
    public void recreateCache() {
        String uri = createUri("admin-user", "password");

        try (RemoteCacheManager manager = new RemoteCacheManager(uri)) {
            RemoteCacheManagerAdmin admin = manager.administration();

            org.infinispan.configuration.cache.Configuration cacheConfiguration =
                    new org.infinispan.configuration.cache.ConfigurationBuilder()
                            .clustering()
                            .cacheMode(org.infinispan.configuration.cache.CacheMode.DIST_SYNC)
                            .encoding().key().mediaType("application/x-protostream")
                            .encoding().value().mediaType("application/x-protostream")
                            .build();

            assertThat(admin.createCache("distCache", cacheConfiguration))
                    .isNotNull()
                    .isInstanceOf(RemoteCache.class);

            assertThatThrownBy(() -> admin.createCache("distCache", cacheConfiguration))
                    .hasMessage("org.infinispan.commons.CacheConfigurationException: ISPN000507: Cache distCache already exists")
                    .isInstanceOf(HotRodClientException.class);

            admin.removeCache("distCache");
        }
    }

    @Test
    public void createOrGetCache() {
        String uri = createUri("admin-user", "password");

        try (RemoteCacheManager manager = new RemoteCacheManager(uri)) {
            RemoteCacheManagerAdmin admin = manager.administration();

            org.infinispan.configuration.cache.Configuration cacheConfiguration =
                    new org.infinispan.configuration.cache.ConfigurationBuilder()
                            .clustering()
                            .cacheMode(org.infinispan.configuration.cache.CacheMode.DIST_SYNC)
                            .encoding().key().mediaType("application/x-protostream")
                            .encoding().value().mediaType("application/x-protostream")
                            .build();

            assertThat(admin.createCache("distCache", cacheConfiguration))
                    .isNotNull()
                    .isInstanceOf(RemoteCache.class);
            assertThat(admin.getOrCreateCache("distCache", cacheConfiguration))
                    .isNotNull()
                    .isInstanceOf(RemoteCache.class);

            admin.removeCache("distCache");

            assertThat(admin.getOrCreateCache("distCache", cacheConfiguration))
                    .isNotNull()
                    .isInstanceOf(RemoteCache.class);

            admin.removeCache("distCache");
        }
    }

    @Test
    public void createVariousCache() {
        String uri = createUri("admin-user", "password");

        try (RemoteCacheManager manager = new RemoteCacheManager(uri)) {
            RemoteCacheManagerAdmin admin = manager.administration();

            org.infinispan.configuration.cache.Configuration distCacheConfiguration =
                    new org.infinispan.configuration.cache.ConfigurationBuilder()
                            .clustering()
                            .cacheMode(org.infinispan.configuration.cache.CacheMode.DIST_SYNC)
                            .encoding().key().mediaType("application/x-protostream")
                            .encoding().value().mediaType("application/x-protostream")
                            .build();

            org.infinispan.configuration.cache.Configuration replCacheConfiguration =
                    new org.infinispan.configuration.cache.ConfigurationBuilder()
                            .clustering()
                            .cacheMode(org.infinispan.configuration.cache.CacheMode.REPL_SYNC)
                            .encoding().key().mediaType("application/x-protostream")
                            .encoding().value().mediaType("application/x-protostream")
                            .build();


            RemoteCache<String, String> distCache = admin.getOrCreateCache("distCache", distCacheConfiguration);
            RemoteCache<String, String> replCache = admin.getOrCreateCache("replCache", replCacheConfiguration);

            IntStream.rangeClosed(1, 100).forEach(i -> distCache.put("key" + i, "value" + i));
            assertThat(distCache.size()).isEqualTo(100);

            IntStream.rangeClosed(1, 100).forEach(i -> replCache.put("key" + i, "value" + i));
            assertThat(replCache.size()).isEqualTo(100);

            admin.removeCache("distCache");
            admin.removeCache("replCache");
        }
    }

    @Test
    public void createCacheByString() {
        String uri = createUri("admin-user", "password");

        try (RemoteCacheManager manager = new RemoteCacheManager(uri)) {
            RemoteCacheManagerAdmin admin = manager.administration();

            String cacheConfigurationAsString =
                    "        <distributed-cache name=\"distCache\">\n" +
                            "            <encoding>\n" +
                            "                <key media-type=\"application/x-protostream\"/>\n" +
                            "                <value media-type=\"application/x-protostream\"/>\n" +
                            "            </encoding>\n" +
                            "        </distributed-cache>";

            org.infinispan.commons.configuration.XMLStringConfiguration cacheConfiguration =
                    new org.infinispan.commons.configuration.XMLStringConfiguration(cacheConfigurationAsString);

            RemoteCache<String, String> distCache = admin.getOrCreateCache("distCache", cacheConfiguration);

            IntStream.rangeClosed(1, 100).forEach(i -> distCache.put("key" + i, "value" + i));
            assertThat(distCache.size()).isEqualTo(100);

            admin.removeCache("distCache");

            assertThatThrownBy(() -> admin.getOrCreateCache("distributedCache", cacheConfiguration))
                    .hasMessageContaining("org.infinispan.commons.CacheConfigurationException: ISPN005031: The supplied configuration for cache 'distributedCache' is missing a named configuration for it:")
                    .isInstanceOf(HotRodClientException.class);
        }
    }
}
