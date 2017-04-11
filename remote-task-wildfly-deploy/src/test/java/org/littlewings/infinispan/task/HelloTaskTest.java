package org.littlewings.infinispan.task;

import java.util.Collections;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HelloTaskTest {
    @Test
    public void callServerTask() {
        RemoteCacheManager cacheManager =
                new RemoteCacheManager(new ConfigurationBuilder().addServer().host("172.17.0.2").port(11222).build());

        try {
            RemoteCache<String, String> cache = cacheManager.getCache("default");

            String result = cache.execute("hello-task", Collections.emptyMap());
            assertThat(result).isEqualTo("Hello World!!");

            cache.stop();
        } finally {
            cacheManager.stop();
        }
    }
}
