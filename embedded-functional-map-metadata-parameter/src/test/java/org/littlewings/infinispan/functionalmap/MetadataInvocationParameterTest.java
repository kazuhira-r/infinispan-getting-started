package org.littlewings.infinispan.functionalmap;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.infinispan.Cache;
import org.infinispan.commons.api.functional.EntryView;
import org.infinispan.commons.api.functional.FunctionalMap;
import org.infinispan.commons.api.functional.MetaParam;
import org.infinispan.commons.api.functional.Param;
import org.infinispan.functional.impl.FunctionalMapImpl;
import org.infinispan.functional.impl.ReadOnlyMapImpl;
import org.infinispan.functional.impl.WriteOnlyMapImpl;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.fail;

public class MetadataInvocationParameterTest {
    @Test
    public void testSetMetadata() {
        withCache("distCache", 3, (Cache<String, Integer> cache) -> {
            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, i));

            FunctionalMapImpl<String, Integer> functionalMap = FunctionalMapImpl.create(cache.getAdvancedCache());
            FunctionalMap.ReadOnlyMap<String, Integer> readOnlyMap = ReadOnlyMapImpl.create(functionalMap);
            FunctionalMap.WriteOnlyMap<String, Integer> writeOnlyMap = WriteOnlyMapImpl.create(functionalMap);

            CompletableFuture<Void> writeFuture =
                    writeOnlyMap
                            .eval("key20",
                                    100,
                                    (BiConsumer<Integer, EntryView.WriteEntryView<Integer>> & Serializable)
                                            (v, view) -> view.set(v, new MetaParam.MetaLifespan(Duration.ofSeconds(3).toMillis())));
            CompletableFuture<Optional<Integer>> readFuture =
                    writeFuture
                            .thenCompose(v -> readOnlyMap.eval("key20", EntryView.ReadEntryView::find));

            assertThat(readFuture.join())
                    .hasValue(100);

            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                // ignore
            }

            assertThat(readOnlyMap.eval("key20", EntryView.ReadEntryView::find).join())
                    .isEmpty();
        });
    }

    @Test
    public void testMetadataMaxIdle() {
        withCache("distCache", 3, (Cache<String, Integer> cache) -> {
            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, i));

            FunctionalMapImpl<String, Integer> functionalMap = FunctionalMapImpl.create(cache.getAdvancedCache());
            FunctionalMap.ReadOnlyMap<String, Integer> readOnlyMap = ReadOnlyMapImpl.create(functionalMap);
            FunctionalMap.WriteOnlyMap<String, Integer> writeOnlyMap = WriteOnlyMapImpl.create(functionalMap);

            CompletableFuture<Void> writeFuture =
                    writeOnlyMap
                            .eval("key20",
                                    100,
                                    (BiConsumer<Integer, EntryView.WriteEntryView<Integer>> & Serializable)
                                            (v, view) -> view.set(v, new MetaParam.MetaMaxIdle(Duration.ofSeconds(3).toMillis())));
            CompletableFuture<Optional<Integer>> readFuture =
                    writeFuture
                            .thenCompose(v -> readOnlyMap.eval("key20", EntryView.ReadEntryView::find));

            assertThat(readFuture.join())
                    .hasValue(100);

            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                // ignore
            }

            assertThat(readOnlyMap.eval("key20", EntryView.ReadEntryView::find).join())
                    .isEmpty();
        });
    }

    @Test
    public void testFindMetadata() {
        withCache("distCache", 3, (Cache<String, Integer> cache) -> {
            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, i));

            FunctionalMapImpl<String, Integer> functionalMap = FunctionalMapImpl.create(cache.getAdvancedCache());
            FunctionalMap.ReadOnlyMap<String, Integer> readOnlyMap = ReadOnlyMapImpl.create(functionalMap);
            FunctionalMap.WriteOnlyMap<String, Integer> writeOnlyMap = WriteOnlyMapImpl.create(functionalMap);

            writeOnlyMap.eval("key20",
                    20,
                    (BiConsumer<Integer, EntryView.WriteEntryView<Integer>> & Serializable)
                            (v, view) -> view.set(v,
                                    new MetaParam.MetaMaxIdle(Duration.ofSeconds(3).toMillis()),
                                    new MetaParam.MetaLifespan(Duration.ofSeconds(5).toMillis()))).join();

            CompletableFuture<Optional<MetaParam.MetaEntryVersion>> readFuture1 =
                    readOnlyMap.eval("key20",
                            view -> view.findMetaParam(MetaParam.MetaEntryVersion.class));

            assertThat(readFuture1.join())
                    .isEmpty();

            CompletableFuture<Optional<MetaParam.MetaEntryVersion<Long>>> readFuture2 =
                    readOnlyMap.eval("key20", view -> view.findMetaParam(MetaParam.MetaEntryVersion.type()));

            assertThat(readFuture2.join())
                    .isEmpty();

            CompletableFuture<Optional<MetaParam.MetaMaxIdle>> readFuture3 =
                    readOnlyMap.eval("key20", view -> view.findMetaParam(MetaParam.MetaMaxIdle.class));

            assertThat(readFuture3.join())
                    .hasValue(new MetaParam.MetaMaxIdle(Duration.ofSeconds(3).toMillis()));

            CompletableFuture<Optional<MetaParam.MetaLifespan>> readFuture4 =
                    readOnlyMap.eval("key20", view -> view.findMetaParam(MetaParam.MetaLifespan.class));

            assertThat(readFuture4.join())
                    .hasValue(new MetaParam.MetaLifespan(Duration.ofSeconds(5).toMillis()));
        });
    }

    @Test
    public void testInvocationParameterFuture() {
        withCache("distCache", 3, (Cache<String, Integer> cache) -> {
            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, i));

            FunctionalMapImpl<String, Integer> functionalMap = FunctionalMapImpl.create(cache.getAdvancedCache());
            FunctionalMap.ReadOnlyMap<String, Integer> readOnlyMap = ReadOnlyMapImpl.create(functionalMap);

            try {
                assertThat(readOnlyMap
                        .withParams(Param.FutureMode.COMPLETED)
                        .eval("key10", view -> {
                            try {
                                TimeUnit.SECONDS.sleep(5);
                            } catch (InterruptedException e) {
                                // ignore
                            }
                            return view.find();
                        })
                        .get(1, TimeUnit.SECONDS))
                        .hasValue(10);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                fail();
            }

            assertThatThrownBy(() ->
                    readOnlyMap
                            .withParams(Param.FutureMode.ASYNC)
                            .eval("key10", view -> {
                                try {
                                    TimeUnit.SECONDS.sleep(5);
                                    System.out.println("sleeped!");
                                } catch (InterruptedException e) {
                                    // ignore
                                }
                                return view.find();
                            })
                            .get(1, TimeUnit.SECONDS))
                    .isInstanceOf(TimeoutException.class);

            assertThatThrownBy(() ->
                    readOnlyMap
                            .eval("key10", view -> {
                                try {
                                    TimeUnit.SECONDS.sleep(5);
                                    System.out.println("sleeped!");
                                } catch (InterruptedException e) {
                                    // ignore
                                }
                                return view.find();
                            })
                            .get(1, TimeUnit.SECONDS))
                    .isInstanceOf(TimeoutException.class);
        });
    }

    @Test
    public void testInvocationParameterPersistence() {
        withCache("distCache", 3, (Cache<String, Integer> cache) -> {
            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, i));

            FunctionalMapImpl<String, Integer> functionalMap = FunctionalMapImpl.create(cache.getAdvancedCache());
            FunctionalMap.WriteOnlyMap<String, Integer> writeOnlyMap = WriteOnlyMapImpl.create(functionalMap);

            writeOnlyMap
                    .withParams(Param.PersistenceMode.SKIP)
                    .eval("key20",
                            20,
                            (BiConsumer<Integer, EntryView.WriteEntryView<Integer>> & Serializable)
                                    (v, view) -> view.set(v)).join();

            writeOnlyMap
                    .withParams(Param.PersistenceMode.PERSIST)
                    .eval("key30",
                            30,
                            (BiConsumer<Integer, EntryView.WriteEntryView<Integer>> & Serializable)
                                    (v, view) -> view.set(v)).join();

            assertThat(cache.get("key20"))
                    .isEqualTo(20);

            assertThat(cache.get("key30"))
                    .isEqualTo(30);

        });
    }

    protected <K, V> void withCache(String cacheName, Consumer<Cache<K, V>> consumer) {
        withCache(cacheName, 1, consumer);
    }

    protected <K, V> void withCache(String cacheName, int numInstances, Consumer<Cache<K, V>> consumer) {
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

        managers.forEach(manager -> manager.getCache(cacheName));

        try {
            consumer.accept(managers.get(0).getCache(cacheName));
        } finally {
            managers.forEach(EmbeddedCacheManager::stop);
        }
    }
}
