package org.littlewings.infinispan.remote.health;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.health.CacheHealth;
import org.infinispan.health.ClusterHealth;
import org.infinispan.health.Health;
import org.infinispan.health.HealthStatus;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EmbeddedCacheManagerHealthTest {
    //@Test
    public void health() throws IOException {
        GlobalConfiguration globalConfiguration =
                new GlobalConfigurationBuilder().clusteredDefault().transport().defaultTransport().build();
        try (EmbeddedCacheManager cacheManager = new DefaultCacheManager(globalConfiguration)) {
            cacheManager
                    .defineConfiguration(
                            "myCache",
                            new ConfigurationBuilder().clustering().cacheMode(CacheMode.DIST_SYNC).build()
                    );
            cacheManager
                    .defineConfiguration(
                            "myCache2",
                            new ConfigurationBuilder().clustering().cacheMode(CacheMode.DIST_SYNC).build()
                    );

            Health health = cacheManager.getHealth();
            ClusterHealth clusterHealth = health.getClusterHealth();
            assertThat(clusterHealth.getHealthStatus()).isEqualTo(HealthStatus.HEALTHY);
            assertThat(clusterHealth.getNumberOfNodes()).isEqualTo(1);

            List<CacheHealth> cacheHealths =
                    health.getCacheHealth().stream().sorted(Comparator.comparing(c -> c.getCacheName())).toList();
            assertThat(cacheHealths).hasSize(2);
            assertThat(cacheHealths.get(0).getCacheName()).isEqualTo("myCache");
            assertThat(cacheHealths.get(0).getStatus()).isEqualTo(HealthStatus.HEALTHY);
            assertThat(cacheHealths.get(1).getCacheName()).isEqualTo("myCache2");
            assertThat(cacheHealths.get(1).getStatus()).isEqualTo(HealthStatus.HEALTHY);
        }
    }
}
