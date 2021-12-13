package org.littlewings.infinispan.remote.metrics;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.RemoteCacheManagerAdmin;
import org.junit.jupiter.api.Test;

public class RemoveCacheTest {
    @Test
    public void removeAllCache() {
        String uri = MetricsTest.createUri("admin-user", "password");

        try (RemoteCacheManager manager = new RemoteCacheManager(uri)) {
            RemoteCacheManagerAdmin admin = manager.administration();

            admin.removeCache("distCache");
            admin.removeCache("replCache");
        }
    }
}
