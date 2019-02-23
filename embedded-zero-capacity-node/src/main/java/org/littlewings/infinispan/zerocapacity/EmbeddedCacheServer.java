package org.littlewings.infinispan.zerocapacity;

import java.io.IOException;

import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

public class EmbeddedCacheServer {
    public static void main(String... args) throws IOException {
        EmbeddedCacheManager manager = new DefaultCacheManager("infinispan.xml");

        manager.getCache("localCache");
        manager.getCache("distributedCache");
        manager.getCache("replicatedCache");

        System.console().readLine("> Enter stop.");

        manager.stop();
    }
}
