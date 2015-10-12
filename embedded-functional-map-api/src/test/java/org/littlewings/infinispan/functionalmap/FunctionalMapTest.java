package org.littlewings.infinispan.functionalmap;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.assertj.core.data.MapEntry;
import org.infinispan.Cache;
import org.infinispan.commons.api.functional.EntryView;
import org.infinispan.commons.api.functional.FunctionalMap;
import org.infinispan.commons.api.functional.Traversable;
import org.infinispan.functional.impl.FunctionalMapImpl;
import org.infinispan.functional.impl.ReadOnlyMapImpl;
import org.infinispan.functional.impl.WriteOnlyMapImpl;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.stream.CacheCollectors;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.infinispan.factories.KnownComponentNames.ASYNC_OPERATIONS_EXECUTOR;

public class FunctionalMapTest {
    @Test
    public void testSimpleReadOnlyMap1() {
        withCache("distCache", 3, (Cache<String, Integer> cache) -> {
            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, i));

            FunctionalMapImpl<String, Integer> functionalMap = FunctionalMapImpl.create(cache.getAdvancedCache());
            FunctionalMap.ReadOnlyMap<String, Integer> readOnlyMap = ReadOnlyMapImpl.create(functionalMap);

            CompletableFuture<Optional<Integer>> readFuture1 =
                    readOnlyMap.eval("key10", EntryView.ReadEntryView::find);

            assertThat(readFuture1.join())
                    .hasValue(10);

            //////////////////////////////////////////////////////////////////////

            CompletableFuture<Optional<Integer>> readFuture2 =
                    readOnlyMap.eval("key20", EntryView.ReadEntryView::find);

            assertThat(readFuture2.join())
                    .isEmpty();
        });
    }

    @Test
    public void testSimpleReadOnlyMap2() {
        withCache("distCache", 3, (Cache<String, Integer> cache) -> {
            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, i));

            FunctionalMapImpl<String, Integer> functionalMap = FunctionalMapImpl.create(cache.getAdvancedCache());
            FunctionalMap.ReadOnlyMap<String, Integer> readOnlyMap = ReadOnlyMapImpl.create(functionalMap);

            CompletableFuture<Integer> readFuture1 =
                    readOnlyMap.eval("key10", EntryView.ReadEntryView::get);

            assertThat(readFuture1.join())
                    .isEqualTo(10);

            //////////////////////////////////////////////////////////////////////

            CompletableFuture<Integer> readFuture2 =
                    readOnlyMap.eval("key20", EntryView.ReadEntryView::get);

            assertThatThrownBy(() -> readFuture2.join())
                    .isInstanceOf(CompletionException.class)
                    .hasCauseInstanceOf(NoSuchElementException.class)
                    .hasMessage("java.util.NoSuchElementException: No value");
        });
    }

    @Test
    public void testSimpleReadOnlyMap3() {
        withCache("distCache", 3, (Cache<String, Integer> cache) -> {
            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, i));

            FunctionalMapImpl<String, Integer> functionalMap = FunctionalMapImpl.create(cache.getAdvancedCache());
            FunctionalMap.ReadOnlyMap<String, Integer> readOnlyMap = ReadOnlyMapImpl.create(functionalMap);

            CompletableFuture<String> readFuture1 =
                    readOnlyMap.eval("key10", EntryView.ReadEntryView::key);

            assertThat(readFuture1.join())
                    .isEqualTo("key10");

            //////////////////////////////////////////////////////////////////////

            CompletableFuture<String> readFuture2 =
                    readOnlyMap.eval("key20", EntryView.ReadEntryView::key);

            assertThat(readFuture2.join())
                    .isEqualTo("key20");
        });
    }

    @Test
    public void testSimpleReadOnlyMap4() {
        withCache("distCache", 3, (Cache<String, Integer> cache) -> {
            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, i));

            FunctionalMapImpl<String, Integer> functionalMap = FunctionalMapImpl.create(cache.getAdvancedCache());
            FunctionalMap.ReadOnlyMap<String, Integer> readOnlyMap = ReadOnlyMapImpl.create(functionalMap);

            Set<String> keys1 = new HashSet<>(Arrays.asList("key1", "key2", "key10"));

            Traversable<Integer> entries1 =
                    readOnlyMap.evalMany(keys1, EntryView.ReadEntryView::get);

            List<Integer> values1 = entries1.collect(Collectors.toList());

            assertThat(values1)
                    .containsOnly(1, 2, 10);

            //////////////////////////////////////////////////////////////////////

            Set<String> keys2 = new HashSet<>(Arrays.asList("key1", "key2", "key20"));

            Traversable<Optional<Integer>> entries2 =
                    readOnlyMap.evalMany(keys2, EntryView.ReadEntryView::find);

            /*
            List<Integer> values2 =
                    StreamSupport
                            .stream(Spliterators.spliteratorUnknownSize(Traversables.asIterator(entries2), Spliterator.ORDERED), false)
                            .flatMap(optional -> optional.map(Stream::of).orElseGet(Stream::empty))
                            .collect(Collectors.toList());
                            */

            List<Integer> values2 =
                    entries2
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());

            /*
            List<Integer> values2 =
                    entries2
                            .collect(ArrayList::new,
                                    (list, optional) -> optional.ifPresent(list::add),
                                    ArrayList::addAll);
                                    */

            assertThat(values2)
                    .containsOnly(1, 2);

            //////////////////////////////////////////////////////////////////////

            CompletableFuture<Optional<Integer>> readFuture1 =
                    readOnlyMap.eval("key1", EntryView.ReadEntryView::find);
            CompletableFuture<Optional<Integer>> readFuture2 =
                    readOnlyMap.eval("key2", EntryView.ReadEntryView::find);

            CompletableFuture<Integer> resultFuture =
                    readFuture1.thenCombineAsync(readFuture2, (optionV1, optionV2) -> optionV1.orElse(0) + optionV2.orElse(0));

            assertThat(resultFuture.join())
                    .isEqualTo(3);
        });
    }

    @Test
    public void testSimpleReadOnlyMap5() {
        withCache("distCache", 3, (Cache<String, Integer> cache) -> {
            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, i));

            FunctionalMapImpl<String, Integer> functionalMap = FunctionalMapImpl.create(cache.getAdvancedCache());
            FunctionalMap.ReadOnlyMap<String, Integer> readOnlyMap = ReadOnlyMapImpl.create(functionalMap);

            Traversable<String> keys = readOnlyMap.keys();
            Traversable<EntryView.ReadEntryView<String, Integer>> entries = readOnlyMap.entries();

            List<String> keysAsList =
                    keys
                            .collect(CacheCollectors.serializableCollector(() -> Collectors.toList()));

            Map<String, Integer> entriesAsMap =
                    entries
                            .collect(Collectors.toMap(EntryView.ReadEntryView::key, EntryView.ReadEntryView::get));

            assertThat(keysAsList)
                    .containsOnly("key1", "key2", "key3", "key4", "key5",
                            "key6", "key7", "key8", "key9", "key10");

            assertThat(entriesAsMap)
                    .containsOnly(MapEntry.entry("key1", 1),
                            MapEntry.entry("key2", 2),
                            MapEntry.entry("key3", 3),
                            MapEntry.entry("key4", 4),
                            MapEntry.entry("key5", 5),
                            MapEntry.entry("key6", 6),
                            MapEntry.entry("key7", 7),
                            MapEntry.entry("key8", 8),
                            MapEntry.entry("key9", 9),
                            MapEntry.entry("key10", 10));
        });
    }

    @Test
    public void testSimpleWriteOnlyMap1() {
        withCache("distCache", 3, (Cache<String, Integer> cache) -> {
            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, i));

            FunctionalMapImpl<String, Integer> functionalMap = FunctionalMapImpl.create(cache.getAdvancedCache());
            FunctionalMap.WriteOnlyMap<String, Integer> writeOnlyMap = WriteOnlyMapImpl.create(functionalMap);
            FunctionalMap.ReadOnlyMap<String, Integer> readOnlyMap = ReadOnlyMapImpl.create(functionalMap);

            CompletableFuture<Void> writeFuture1 =
                    writeOnlyMap.eval("key1",
                            (Consumer<EntryView.WriteEntryView<Integer>> & Serializable) view -> view.set(10));

            CompletableFuture<Optional<Integer>> readFuture1 =
                    writeFuture1.thenCompose(r -> readOnlyMap.eval("key1", EntryView.ReadEntryView::find));

            assertThat(readFuture1.join())
                    .hasValue(10);

            //////////////////////////////////////////////////////////////////////

            CompletableFuture<Void> writeFuture2 =
                    writeOnlyMap.eval("key1", 20,
                            (BiConsumer<Integer, EntryView.WriteEntryView<Integer>> & Serializable) (v, view) -> view.set(v));

            CompletableFuture<Optional<Integer>> readFuture2 =
                    writeFuture2.thenCompose(r -> readOnlyMap.eval("key1", EntryView.ReadEntryView::find));

            assertThat(readFuture2.join())
                    .hasValue(20);

            //////////////////////////////////////////////////////////////////////

            CompletableFuture<Void> writeFuture3 =
                    writeOnlyMap.eval("key20",
                            (Consumer<EntryView.WriteEntryView<Integer>> & Serializable) view -> view.set(20));

            CompletableFuture<Optional<Integer>> readFuture3 =
                    writeFuture3.thenCompose(r -> readOnlyMap.eval("key20", EntryView.ReadEntryView::find));

            assertThat(readFuture3.join())
                    .hasValue(20);
        });
    }

    @Test
    public void testSimpleWriteOnlyMap2() {
        withCache("distCache", 3, (Cache<String, Integer> cache) -> {
            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, i));

            FunctionalMapImpl<String, Integer> functionalMap = FunctionalMapImpl.create(cache.getAdvancedCache());
            FunctionalMap.WriteOnlyMap<String, Integer> writeOnlyMap = WriteOnlyMapImpl.create(functionalMap);
            FunctionalMap.ReadOnlyMap<String, Integer> readOnlyMap = ReadOnlyMapImpl.create(functionalMap);

            Set<String> keys = new HashSet<>(Arrays.asList("key1", "key2", "key10"));

            CompletableFuture<Void> writeFuture1 =
                    writeOnlyMap.evalMany(keys,
                            (Consumer<EntryView.WriteEntryView<Integer>> & Serializable) view -> view.set(100));

            CompletableFuture<Traversable<Optional<Integer>>> readFuture1 =
                    writeFuture1.thenCompose(r ->
                            CompletableFuture.supplyAsync(() ->
                                    readOnlyMap.<Optional<Integer>>evalMany(keys, EntryView.ReadEntryView::find)));
            List<Integer> entries1 =
                    readFuture1
                            .join()
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());

            assertThat(entries1)
                    .containsOnly(100, 100, 100);

            //////////////////////////////////////////////////////////////////////

            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, i));

            ExecutorService executorService =
                    cache
                            .getAdvancedCache()
                            .getComponentRegistry()
                            .getComponent(ExecutorService.class, ASYNC_OPERATIONS_EXECUTOR);

            Map<String, Integer> inputEntries = new HashMap<>();
            inputEntries.put("key1", 500);
            inputEntries.put("key20", 1000);

            CompletableFuture<Void> writeFuture2 =
                    writeOnlyMap.evalMany(inputEntries,
                            (BiConsumer<Integer, EntryView.WriteEntryView<Integer>> & Serializable) (v, view) -> view.set(v));

            Set<String> targetKeys = new HashSet<>(Arrays.asList("key1", "key2", "key10", "key20"));

            CompletableFuture<Traversable<Optional<Integer>>> readFuture2 =
                    writeFuture2.thenCompose(r ->
                            CompletableFuture.supplyAsync(() ->
                                            readOnlyMap.<Optional<Integer>>evalMany(targetKeys, EntryView.ReadEntryView::find),
                                    executorService));

            List<Integer> entries2 =
                    readFuture2
                            .join()
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());

            assertThat(entries2)
                    .containsOnly(500, 2, 10, 1000);
        });
    }

    @Test
    public void testSimpleWriteOnlyMap3() {
        withCache("distCache", 3, (Cache<String, Integer> cache) -> {
            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, i));

            FunctionalMapImpl<String, Integer> functionalMap = FunctionalMapImpl.create(cache.getAdvancedCache());
            FunctionalMap.WriteOnlyMap<String, Integer> writeOnlyMap = WriteOnlyMapImpl.create(functionalMap);
            FunctionalMap.ReadOnlyMap<String, Integer> readOnlyMap = ReadOnlyMapImpl.create(functionalMap);

            CompletableFuture<Void> removeFuture =
                    writeOnlyMap.eval("key1",
                            (Consumer<EntryView.WriteEntryView<Integer>> & Serializable) EntryView.WriteEntryView::remove);
            CompletableFuture<Optional<Integer>> readFuture1 =
                    removeFuture.thenCompose(r -> readOnlyMap.eval("key1", EntryView.ReadEntryView::find));

            assertThat(readFuture1.join())
                    .isEmpty();

            //////////////////////////////////////////////////////////////////////

            ExecutorService executorService =
                    cache
                            .getAdvancedCache()
                            .getComponentRegistry()
                            .getComponent(ExecutorService.class, ASYNC_OPERATIONS_EXECUTOR);

            Set<String> targetKeys = new HashSet<>(Arrays.asList("key2", "key10", "key20"));

            CompletableFuture<Void> removesFuture =
                    writeOnlyMap.evalMany(targetKeys,
                            (Consumer<EntryView.WriteEntryView<Integer>> & Serializable) EntryView.WriteEntryView::remove);
            CompletableFuture<Traversable<Optional<Integer>>> readFuture2 =
                    removesFuture.thenCompose(r ->
                            CompletableFuture.supplyAsync(() -> readOnlyMap.evalMany(targetKeys, EntryView.ReadEntryView::find),
                                    executorService));

            boolean isEmpty =
                    readFuture2
                            .join()
                            .allMatch(optional -> !optional.isPresent());

            assertThat(isEmpty)
                    .isTrue();
        });
    }

    @Test
    public void testSimpleWriteOnlyMap4() {
        withCache("distCache", 3, (Cache<String, Integer> cache) -> {
            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, i));

            FunctionalMapImpl<String, Integer> functionalMap = FunctionalMapImpl.create(cache.getAdvancedCache());
            FunctionalMap.WriteOnlyMap<String, Integer> writeOnlyMap = WriteOnlyMapImpl.create(functionalMap);

            ExecutorService executorService =
                    cache
                            .getAdvancedCache()
                            .getComponentRegistry()
                            .getComponent(ExecutorService.class, ASYNC_OPERATIONS_EXECUTOR);

            CompletableFuture<Void> allRemoveFuture =
                    writeOnlyMap.evalAll((Consumer<EntryView.WriteEntryView<Integer>> & Serializable) EntryView.WriteEntryView::remove);
            CompletableFuture<Boolean> readFuture1 =
                    allRemoveFuture.thenCompose(r -> CompletableFuture.supplyAsync(() -> cache.isEmpty(),
                            executorService));

            assertThat(readFuture1.join())
                    .isTrue();

            //////////////////////////////////////////////////////////////////////

            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, i));

            CompletableFuture<Void> truncateFuture = writeOnlyMap.truncate();
            CompletableFuture<Boolean> readFuture2 =
                    truncateFuture.thenCompose(r -> CompletableFuture.supplyAsync(() -> cache.isEmpty(),
                            executorService));

            assertThat(readFuture2.join())
                    .isTrue();
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
        List<Cache<K, V>> caches =
                managers
                        .stream()
                        .map(m -> m.<K, V>getCache(cacheName))
                        .collect(Collectors.toList());

        try {
            consumer.accept(caches.get(0));
        } finally {
            caches.forEach(Cache::stop);
            managers.forEach(EmbeddedCacheManager::stop);
        }
    }
}
