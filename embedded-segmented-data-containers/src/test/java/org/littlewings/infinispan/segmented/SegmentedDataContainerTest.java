package org.littlewings.infinispan.segmented;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.infinispan.Cache;
import org.infinispan.CacheStream;
import org.infinispan.commons.util.IntSet;
import org.infinispan.commons.util.IntSets;
import org.infinispan.configuration.cache.HashConfiguration;
import org.infinispan.container.DataContainer;
import org.infinispan.container.impl.BoundedSegmentedDataContainer;
import org.infinispan.container.impl.DefaultDataContainer;
import org.infinispan.container.impl.DefaultSegmentedDataContainer;
import org.infinispan.container.impl.InternalDataContainer;
import org.infinispan.container.impl.L1SegmentedDataContainer;
import org.infinispan.container.offheap.BoundedOffHeapDataContainer;
import org.infinispan.container.offheap.OffHeapDataContainer;
import org.infinispan.container.offheap.SegmentedBoundedOffHeapDataContainer;
import org.infinispan.distribution.DistributionInfo;
import org.infinispan.distribution.DistributionManager;
import org.infinispan.distribution.ch.impl.HashFunctionPartitioner;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.Address;
import org.infinispan.stream.CacheCollectors;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SegmentedDataContainerTest {
    @Test
    public void dataContainersDefaultSegmentedEnabled() {
        this.<String, Integer>withCache("defaultCache", 3, cache -> {
            EmbeddedCacheManager manager = cache.getCacheManager();

            // On-Heap
            DataContainer<String, Integer> defaultHeapDataContainer =
                    manager.<String, Integer>getCache("defaultCache").getAdvancedCache().getDataContainer();
            assertThat(defaultHeapDataContainer)
                    .isInstanceOf(DefaultSegmentedDataContainer.class);

            DataContainer<String, Integer> l1HeapDataContainer =
                    manager.<String, Integer>getCache("l1Cache").getAdvancedCache().getDataContainer();
            assertThat(l1HeapDataContainer)
                    .isInstanceOf(L1SegmentedDataContainer.class);

            DataContainer<String, Integer> sizeBoundedHeapDataContainer =
                    manager.<String, Integer>getCache("sizeBoundedCache").getAdvancedCache().getDataContainer();
            assertThat(sizeBoundedHeapDataContainer)
                    .isInstanceOf(BoundedSegmentedDataContainer.class);

            DataContainer<String, Integer> sizeBoundedL1HeapDataContainer =
                    manager.<String, Integer>getCache("sizeBoundedL1Cache").getAdvancedCache().getDataContainer();
            assertThat(sizeBoundedL1HeapDataContainer)
                    .isInstanceOf(BoundedSegmentedDataContainer.class);

            // Off-Heap
            DataContainer<String, Integer> defaultOffHeapDataContainer =
                    manager.<String, Integer>getCache("defaultOffHeapCache").getAdvancedCache().getDataContainer();
            assertThat(defaultOffHeapDataContainer)
                    .isInstanceOf(DefaultSegmentedDataContainer.class);

            DataContainer<String, Integer> l1OffHeapDataContainer =
                    manager.<String, Integer>getCache("l1OffHeapCache").getAdvancedCache().getDataContainer();
            assertThat(l1OffHeapDataContainer)
                    .isInstanceOf(L1SegmentedDataContainer.class);

            DataContainer<String, Integer> sizeBoundedOffHeapDataContainer =
                    manager.<String, Integer>getCache("sizeBoundedOffHeapCache").getAdvancedCache().getDataContainer();
            assertThat(sizeBoundedOffHeapDataContainer)
                    .isInstanceOf(SegmentedBoundedOffHeapDataContainer.class);

            DataContainer<String, Integer> sizeBoundedL1OffHeapDataContainer =
                    manager.<String, Integer>getCache("sizeBoundedL1OffHeapCache").getAdvancedCache().getDataContainer();
            assertThat(sizeBoundedL1OffHeapDataContainer)
                    .isInstanceOf(SegmentedBoundedOffHeapDataContainer.class);
        });
    }

    @Test
    public void dataContainersSegmentedDisabled() {
        System.setProperty("org.infinispan.feature.data-segmentation", "false");

        this.<String, Integer>withCache("defaultCache", 3, cache -> {
            EmbeddedCacheManager manager = cache.getCacheManager();

            // On-Heap
            DataContainer<String, Integer> defaultHeapDataContainer =
                    manager.<String, Integer>getCache("defaultCache").getAdvancedCache().getDataContainer();
            assertThat(defaultHeapDataContainer)
                    .isInstanceOf(DefaultDataContainer.class);

            DataContainer<String, Integer> l1HeapDataContainer =
                    manager.<String, Integer>getCache("l1Cache").getAdvancedCache().getDataContainer();
            assertThat(l1HeapDataContainer)
                    .isInstanceOf(DefaultDataContainer.class);

            DataContainer<String, Integer> sizeBoundedHeapDataContainer =
                    manager.<String, Integer>getCache("sizeBoundedCache").getAdvancedCache().getDataContainer();
            assertThat(sizeBoundedHeapDataContainer)
                    .isInstanceOf(DefaultDataContainer.class);

            DataContainer<String, Integer> sizeBoundedL1HeapDataContainer =
                    manager.<String, Integer>getCache("sizeBoundedL1Cache").getAdvancedCache().getDataContainer();
            assertThat(sizeBoundedL1HeapDataContainer)
                    .isInstanceOf(DefaultDataContainer.class);

            /*
            // Off-Heap
            DataContainer<String, Integer> defaultOffHeapDataContainer =
                    manager.<String, Integer>getCache("defaultOffHeapCache").getAdvancedCache().getDataContainer();
            assertThat(defaultOffHeapDataContainer)
                    .isInstanceOf(OffHeapDataContainer.class);

            DataContainer<String, Integer> l1OffHeapDataContainer =
                    manager.<String, Integer>getCache("l1OffHeapCache").getAdvancedCache().getDataContainer();
            assertThat(l1OffHeapDataContainer)
                    .isInstanceOf(BoundedOffHeapDataContainer.class);

            DataContainer<String, Integer> sizeBoundedOffHeapDataContainer =
                    manager.<String, Integer>getCache("sizeBoundedOffHeapCache").getAdvancedCache().getDataContainer();
            assertThat(sizeBoundedOffHeapDataContainer)
                    .isInstanceOf(BoundedOffHeapDataContainer.class);

            DataContainer<String, Integer> sizeBoundedL1OffHeapDataContainer =
                    manager.<String, Integer>getCache("sizeBoundedL1OffHeapCache").getAdvancedCache().getDataContainer();
            assertThat(sizeBoundedL1OffHeapDataContainer)
                    .isInstanceOf(BoundedOffHeapDataContainer.class);
                    */
        });

        System.clearProperty("org.infinispan.feature.data-segmentation");
    }

    @Test
    public void localOrReplicatedOrScatteredCache() {
        this.<String, Integer>withCache("defaultCache", 3, cache -> {
            EmbeddedCacheManager manager = cache.getCacheManager();

            // Local Cache
            Cache<String, Integer> localCache = manager.getCache("localCache");
            assertThat(localCache.getAdvancedCache().getDataContainer())
                    .isInstanceOf(DefaultDataContainer.class);

            assertThat(localCache.getCacheConfiguration().clustering().hash().keyPartitioner())
                    .isInstanceOf(HashFunctionPartitioner.class);

            // Replicated Cache
            Cache<String, Integer> replicatedCache = manager.getCache("replicatedCache");
            assertThat(replicatedCache.getAdvancedCache().getDataContainer())
                    .isInstanceOf(DefaultSegmentedDataContainer.class);

            assertThat(replicatedCache.getCacheConfiguration().clustering().hash().keyPartitioner())
                    .isInstanceOf(HashFunctionPartitioner.class);

            // Scattered Cache
            Cache<String, Integer> scatteredCache = manager.getCache("scatteredCache");
            assertThat(scatteredCache.getAdvancedCache().getDataContainer())
                               .isInstanceOf(DefaultSegmentedDataContainer.class);

            assertThat(scatteredCache.getCacheConfiguration().clustering().hash().keyPartitioner())
                    .isInstanceOf(HashFunctionPartitioner.class);
        });
    }

    @Test
    public void defaultSegmentSize() {
        this.<String, Integer>withCache("defaultCache", 3, cache -> {
            assertThat(cache.getCacheConfiguration().clustering().hash().numSegments())
                    .isEqualTo(256);
        });


        System.setProperty("org.infinispan.feature.data-segmentation", "false");

        this.<String, Integer>withCache("defaultCache", 3, cache -> {
            assertThat(cache.getCacheConfiguration().clustering().hash().numSegments())
                    .isEqualTo(256);
        });

        System.clearProperty("org.infinispan.feature.data-segmentation");
    }

    @Test
    public void segments() {
        this.<String, Integer>withCache("defaultCache", 3, cache -> {

            HashConfiguration hashConfiguration = cache.getCacheConfiguration().clustering().hash();

            // Segmentの数（デフォルト）
            assertThat(hashConfiguration.numSegments())
                    .isEqualTo(256);

            // 設定されているKeyPartitionerの確認（デフォルト）
            assertThat(hashConfiguration.keyPartitioner())
                    .isInstanceOf(HashFunctionPartitioner.class);

            IntStream.rangeClosed(1, 1000).forEach(i -> cache.put("key" + i, i));

            // データの配置を確認するのに、DistributionManagerを使う
            DistributionManager dm = cache.getAdvancedCache().getDistributionManager();

            // キーの配置状況の確認
            cache.forEach((k, v) -> {
                int segmentId = dm.getCacheTopology().getSegment(k);
                DistributionInfo di = dm.getCacheTopology().getSegmentDistribution(segmentId);

                System.out.printf(
                        "key[%s]: segment[%d], primary[%s], backup[%s]%n",
                        k,
                        segmentId,
                        di.primary(),
                        di.writeBackups().stream().map(Address::toString).collect(Collectors.joining(", "))
                );
            });

            // ローカルのSegmentの数
            IntSet segments = dm.getCacheTopology().getLocalReadSegments();
            assertThat(segments.size()).isGreaterThan(150);

            // ローカルのDataContainerが保持しているSegmentに属するエントリ数
            InternalDataContainer<String, Integer> dataContainer =
                    (InternalDataContainer<String, Integer>) cache.getAdvancedCache().getDataContainer();

            assertThat(dataContainer.size(segments)).isGreaterThan(600);

            // あるSegmentに属するCacheEntryを取得
            int anySegmentId = segments.stream().findAny().get();
            System.out.printf("selected segment-id = %d%n", anySegmentId);
            dataContainer
                    .forEach(
                            IntSets.immutableSet(anySegmentId),
                            cacheEntry -> System.out.println(cacheEntry)
                    );

            int segmentSize =
                    dm
                            .getCacheTopology()
                            // クラスタ内の全Memberを取得
                            .getMembersSet()
                            .stream()
                            // PrimaryOwnerが持つSegmentの集合を取得
                            .map(address -> dm.getReadConsistentHash().getPrimarySegmentsForOwner(address))
                            .peek(s -> System.out.println(s.size()))
                            .peek(s -> assertThat(s.size()).isGreaterThan(75))
                            .reduce(0, (acc, cur) -> acc + cur.size(), (acc, cur) -> acc + cur);

            assertThat(segmentSize).isEqualTo(256);
        });
    }

    @Test
     public void nonSegments() {
        System.setProperty("org.infinispan.feature.data-segmentation", "false");

         this.<String, Integer>withCache("defaultCache", 3, cache -> {

             HashConfiguration hashConfiguration = cache.getCacheConfiguration().clustering().hash();

             // Segmentの数（デフォルト）
             assertThat(hashConfiguration.numSegments())
                     .isEqualTo(256);

             // 設定されているKeyPartitionerの確認（デフォルト）
             assertThat(hashConfiguration.keyPartitioner())
                     .isInstanceOf(HashFunctionPartitioner.class);

             IntStream.rangeClosed(1, 1000).forEach(i -> cache.put("key" + i, i));

             // データの配置を確認するのに、DistributionManagerを使う
             DistributionManager dm = cache.getAdvancedCache().getDistributionManager();

             // キーの配置状況の確認
             cache.forEach((k, v) -> {
                 int segmentId = dm.getCacheTopology().getSegment(k);
                 DistributionInfo di = dm.getCacheTopology().getSegmentDistribution(segmentId);

                 System.out.printf(
                         "key[%s]: segment[%d], primary[%s], backup[%s]%n",
                         k,
                         segmentId,
                         di.primary(),
                         di.writeBackups().stream().map(Address::toString).collect(Collectors.joining(", "))
                 );
             });

             // ローカルのSegmentの数
             IntSet segments = dm.getCacheTopology().getLocalReadSegments();
             assertThat(segments.size()).isGreaterThan(150);

             // ローカルのDataContainerが保持しているSegmentに属するエントリ数
             InternalDataContainer<String, Integer> dataContainer =
                     (InternalDataContainer<String, Integer>) cache.getAdvancedCache().getDataContainer();

             assertThat(dataContainer.size(segments)).isGreaterThan(600);

             // あるSegmentに属するCacheEntryを取得
             int anySegmentId = segments.stream().findAny().get();
             System.out.printf("selected segment-id = %d%n", anySegmentId);
             dataContainer
                     .forEach(
                             IntSets.immutableSet(anySegmentId),
                             cacheEntry -> System.out.println(cacheEntry)
                     );

             int segmentSize =
                     dm
                             .getCacheTopology()
                             // クラスタ内の全Memberを取得
                             .getMembersSet()
                             .stream()
                             // PrimaryOwnerが持つSegmentの集合を取得
                             .map(address -> dm.getReadConsistentHash().getPrimarySegmentsForOwner(address))
                             .peek(s -> System.out.println(s.size()))
                             .peek(s -> assertThat(s.size()).isGreaterThan(75))
                             .reduce(0, (acc, cur) -> acc + cur.size(), (acc, cur) -> acc + cur);

             assertThat(segmentSize).isEqualTo(256);
         });

        System.clearProperty("org.infinispan.feature.data-segmentation");
    }

    @Test
    public void segmentedDistributedStreams() {
        this.<String, Integer>withCache("defaultCache", 3, cache -> {
            IntStream.rangeClosed(1, 1000000).forEach(i -> cache.put("key" + i, i));

            try (CacheStream<Map.Entry<String, Integer>> stream = cache.entrySet().stream()) {
                long startTime = System.nanoTime();

                long summarizeResult =
                        stream
                                .map(e -> e.getValue() * 2)
                                .collect(CacheCollectors.serializableCollector(() -> Collectors.summarizingInt(i -> i)))
                                .getSum();

                long elapsedTime = TimeUnit.MILLISECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);

                System.out.println(elapsedTime);
                assertThat(elapsedTime)
                        .isLessThan(180L);

                assertThat(summarizeResult).isEqualTo(1000001000000L);
            }

            try (CacheStream<Map.Entry<String, Integer>> stream = cache.entrySet().stream()) {
                int segmentId =
                        cache.getAdvancedCache().getDistributionManager().getCacheTopology().getSegment("key1");
                assertThat(segmentId).isEqualTo(46);

                long startTime = System.nanoTime();

                long summarizeResult =
                        stream
                                .filterKeySegments(IntSets.immutableSet(segmentId))
                                .map(e -> e.getValue() * 2)
                                .collect(CacheCollectors.serializableCollector(() -> Collectors.summarizingInt(i -> i)))
                                .getSum();

                long elapsedTime = TimeUnit.MILLISECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);

                System.out.println(elapsedTime);
                assertThat(elapsedTime)
                        .isLessThan(10L);

                assertThat(summarizeResult).isEqualTo(3901388570L);
            }
        });
    }

    @Test
    public void nonSegmentedDistributedStreams() {
        System.setProperty("org.infinispan.feature.data-segmentation", "false");

        this.<String, Integer>withCache("defaultCache", 3, cache -> {
            IntStream.rangeClosed(1, 1000000).forEach(i -> cache.put("key" + i, i));

            try (CacheStream<Map.Entry<String, Integer>> stream = cache.entrySet().stream()) {
                long startTime = System.nanoTime();

                long summarizeResult =
                        stream
                                .map(e -> e.getValue() * 2)
                                .collect(CacheCollectors.serializableCollector(() -> Collectors.summarizingInt(i -> i)))
                                .getSum();

                long elapsedTime = TimeUnit.MILLISECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);

                System.out.println(elapsedTime);
                assertThat(elapsedTime)
                        .isLessThan(300L);

                assertThat(summarizeResult).isEqualTo(1000001000000L);
            }

            try (CacheStream<Map.Entry<String, Integer>> stream = cache.entrySet().stream()) {
                int segmentId =
                        cache.getAdvancedCache().getDistributionManager().getCacheTopology().getSegment("key1");
                assertThat(segmentId).isEqualTo(46);

                long startTime = System.nanoTime();

                long summarizeResult =
                        stream
                                .filterKeySegments(IntSets.immutableSet(segmentId))
                                .map(e -> e.getValue() * 2)
                                .collect(CacheCollectors.serializableCollector(() -> Collectors.summarizingInt(i -> i)))
                                .getSum();

                long elapsedTime = TimeUnit.MILLISECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);

                System.out.println(elapsedTime);
                assertThat(elapsedTime)
                        .isLessThan(150L);

                assertThat(summarizeResult).isEqualTo(3901388570L);
            }
        });

        System.clearProperty("org.infinispan.feature.data-segmentation");
    }

    <K, V> void withCache(String cacheName, int numInstances, Consumer<Cache<K, V>> func) {
        List<EmbeddedCacheManager> managers =
                IntStream
                        .rangeClosed(1, numInstances)
                        .mapToObj(i -> {
                            try {
                                return new DefaultCacheManager("infinispan.xml");
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        })
                        .collect(Collectors.toList());

        try {
            managers.forEach(m -> m.getCache(cacheName));

            Cache<K, V> cache = managers.get(0).getCache(cacheName);
            func.accept(cache);
        } finally {
            managers.forEach(m -> m.getCache(cacheName).stop());
            managers.forEach(EmbeddedCacheManager::stop);
        }
    }
}
