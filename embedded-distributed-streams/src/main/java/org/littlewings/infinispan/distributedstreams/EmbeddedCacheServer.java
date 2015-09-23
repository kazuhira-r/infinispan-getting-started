package org.littlewings.infinispan.distributedstreams;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

public class EmbeddedCacheServer {
    public static void main(String... args) throws IOException {
        String cacheName = args[0];

        EmbeddedCacheManager manager = new DefaultCacheManager("infinispan.xml");

        Cache<?, ?> cache  = manager.getCache(cacheName);

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        System.out.printf("[%s] EmbeddedCacheServer startup.%n", now.format(formatter));
        System.console().readLine("enter, stop...");

        manager.stop();
    }
}
