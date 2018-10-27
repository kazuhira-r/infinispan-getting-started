package org.littlewings.infinispan.functionalmap;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import org.infinispan.Cache;
import org.infinispan.distribution.DistributionInfo;
import org.infinispan.distribution.DistributionManager;
import org.infinispan.functional.FunctionalMap;
import org.infinispan.functional.impl.FunctionalMapImpl;
import org.infinispan.functional.impl.ReadOnlyMapImpl;
import org.infinispan.functional.impl.ReadWriteMapImpl;
import org.infinispan.functional.impl.WriteOnlyMapImpl;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.logging.Logger;

public class FunctionalMapRunner {
    public static void main(String... args) throws IOException {
        EmbeddedCacheManager manager = new DefaultCacheManager("infinispan.xml");

        Cache<String, Integer> cache = manager.getCache("distributedCache");

        System.out.printf("server[%s] started.%n", manager.getAddress());

        IntStream.rangeClosed(1, 100).forEach(i -> cache.put("key" + i, i));

        FunctionalMapImpl<String, Integer> functionalMap = FunctionalMapImpl.create(cache.getAdvancedCache());
        FunctionalMap.ReadOnlyMap<String, Integer> readOnlyMap = ReadOnlyMapImpl.create(functionalMap);
        FunctionalMap.WriteOnlyMap<String, Integer> writeOnlyMap = WriteOnlyMapImpl.create(functionalMap);
        FunctionalMap.ReadWriteMap<String, Integer> readWriteMap = ReadWriteMapImpl.create(functionalMap);

        DistributionManager dm = cache.getAdvancedCache().getDistributionManager();

        System.out.println("============ single key ============");

        cache.keySet().forEach(key -> {
            DistributionInfo di = dm.getCacheTopology().getDistribution(key);
            System.out.printf("pattern-1: location / key[%s]: primary[%s] / backup%s%n", key, di.primary(), di.writeBackups());

            readOnlyMap.eval(key, entry -> {
                Logger logger = Logger.getLogger("lambda-logger");
                logger.infof("pattern-1: read-only / key[%s]%n", key);
                return entry.get();
            }).join();

            writeOnlyMap.eval(key, di.primary().toString(), (param, entry) -> {
                Logger logger = Logger.getLogger("lambda-logger");
                logger.infof("pattern-1: write-only / key[%s]: primary = %s%n", key, param);
            }).join();

            readWriteMap.eval(key, di.primary().toString(), (param, entry) -> {
                Logger logger = Logger.getLogger("lambda-logger");
                logger.infof("pattern-1: read-write / key[%s]: primary = %s%n", key, param);
                return null;
            }).join();
        });

        System.out.println("============ many key ============");
        for (int i = 1; i <= 100; i += 4) {
            Set<String> keys = new HashSet<>();
            for (int j = 0; j < 4; j++) {
                if (i + j <= 100) {
                    keys.add("key" + (i + j));
                }
            }

            readOnlyMap.evalMany(keys, entry -> {
                Logger logger = Logger.getLogger("lambda-logger");
                logger.infof("pattern-2: read-only / key[%s]%n", entry.key());
                return entry.get();
            });

            writeOnlyMap.evalMany(keys, entry -> {
                Logger logger = Logger.getLogger("lambda-logger");
                logger.infof("pattern-2: write-only / key[%s]%n", entry.key());
            }).join();

            readWriteMap.evalMany(keys, entry -> {
                Logger logger = Logger.getLogger("lambda-logger");
                logger.infof("pattern-2: read-write / key[%s]%n", entry.key());
                return null;
            });
        }

        cache.stop();
        manager.stop();
    }
}
