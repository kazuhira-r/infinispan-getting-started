package org.littlewings.infinispan.zerocapacity;

import java.io.IOException;
import java.util.stream.IntStream;

import org.infinispan.Cache;
import org.infinispan.distribution.DistributionInfo;
import org.infinispan.distribution.DistributionManager;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

public class Runner {
    public static void main(String... args) throws IOException {
        System.setProperty("enabled.zero.capacity.node", "true");

        EmbeddedCacheManager manager = new DefaultCacheManager("infinispan.xml");

        String cacheName = args[0];
        String command = args[1];

        System.out.printf("self = %s, cache = %s, command = %s%n", manager.getAddress(), cacheName, command);

        Cache<String, String> cache = manager.getCache(cacheName);

        switch (command) {
            case "get":
                DistributionManager distributionManager = cache.getAdvancedCache().getDistributionManager();
                cache.forEach((key, value) -> {
                    DistributionInfo distributionInfo = distributionManager.getCacheTopology().getDistribution(key);

                    System.out.printf(
                            "key / value = [%s / %s], primary = %s, read-owners = %s, write-owners = %s%n",
                            key,
                            value,
                            distributionInfo.primary(),
                            distributionInfo.readOwners(),
                            distributionInfo.writeOwners()
                    );
                });

                break;
            case "put":
                IntStream.rangeClosed(1, 100).forEach(i -> cache.put("key" + i, "value" + i));

                break;
            default:
                throw new IllegalArgumentException("Unknown command[" + command + "]");
        }

        cache.stop();
        manager.stop();

        System.out.println("done!!");
    }
}
