package org.littlewings.infinispan.functionalmap;

import java.io.IOException;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

public class EmbeddedCacheServer {
    public static void main(String... args) throws IOException {
        EmbeddedCacheManager manager = new DefaultCacheManager("infinispan.xml");

        Cache<String, Integer> cache = manager.getCache("distributedCache");

        System.out.printf("server[%s] started.%n", manager.getAddress());

        System.console().readLine("> Enter stop.");

        cache.stop();
        manager.stop();
    }
}
