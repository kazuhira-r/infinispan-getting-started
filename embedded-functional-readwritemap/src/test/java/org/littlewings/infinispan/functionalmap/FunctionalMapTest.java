package org.littlewings.infinispan.functionalmap;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.infinispan.Cache;
import org.infinispan.commons.api.functional.EntryView;
import org.infinispan.commons.api.functional.FunctionalMap;
import org.infinispan.commons.api.functional.Traversable;
import org.infinispan.functional.impl.FunctionalMapImpl;
import org.infinispan.functional.impl.ReadWriteMapImpl;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FunctionalMapTest {
    @Test
    public void testReadWriteMap1() {
        withCache("distCache", 3, (Cache<String, Integer> cache) -> {
            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, i));

            FunctionalMapImpl<String, Integer> functionalMap = FunctionalMapImpl.create(cache.getAdvancedCache());
            FunctionalMap.ReadWriteMap<String, Integer> readWriteMap = ReadWriteMapImpl.create(functionalMap);

            CompletableFuture<Optional<Integer>> previousValueFuture =
                    readWriteMap
                            .eval("key1",
                                    10,
                                    (BiFunction<Integer, EntryView.ReadWriteEntryView<String, Integer>, Optional<Integer>> & Serializable)
                                            (v, view) -> {
                                                Optional<Integer> previous = view.find();
                                                view.set(v);
                                                return previous;
                                            });

            assertThat(previousValueFuture.join())
                    .hasValue(1);
        });
    }

    @Test
    public void testReadWriteMap2() {
        withCache("distCache", 3, (Cache<String, Integer> cache) -> {
            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, i));

            FunctionalMapImpl<String, Integer> functionalMap = FunctionalMapImpl.create(cache.getAdvancedCache());
            FunctionalMap.ReadWriteMap<String, Integer> readWriteMap = ReadWriteMapImpl.create(functionalMap);

            Set<String> keys = new HashSet<>(Arrays.asList("key1", "key5", "key10"));

            Traversable<Optional<Integer>> entries =
                    readWriteMap
                            .evalMany(keys,
                                    (Function<EntryView.ReadWriteEntryView<String, Integer>, Optional<Integer>> & Serializable)
                                            EntryView.ReadWriteEntryView::find);

            List<Integer> results =
                    entries
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());

            assertThat(results)
                    .containsOnly(1, 5, 10);
        });
    }

    @Test
    public void testReadWriteMap3() {
        withCache("distCache", 3, (Cache<String, Integer> cache) -> {
            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, i));

            FunctionalMapImpl<String, Integer> functionalMap = FunctionalMapImpl.create(cache.getAdvancedCache());
            FunctionalMap.ReadWriteMap<String, Integer> readWriteMap = ReadWriteMapImpl.create(functionalMap);

            Traversable<Void> results =
                    readWriteMap
                            .evalAll((Function<EntryView.ReadWriteEntryView<String, Integer>, Void> & Serializable)
                                    EntryView.ReadWriteEntryView::remove);
            results.forEach(r -> {
            });

            assertThat(cache.isEmpty())
                    .isTrue();
        });
    }

    @Test
    public void testReadWriteMapLikeReplace1() {
        withCache("distCache", 3, (Cache<String, Integer> cache) -> {
            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, i));

            FunctionalMapImpl<String, Integer> functionalMap = FunctionalMapImpl.create(cache.getAdvancedCache());
            FunctionalMap.ReadWriteMap<String, Integer> readWriteMap = ReadWriteMapImpl.create(functionalMap);

            int oldValue = 10;

            CompletableFuture<Optional<Integer>> replaceFuture =
                    readWriteMap
                            .eval(
                                    "key10",
                                    100,
                                    (BiFunction<Integer, EntryView.ReadWriteEntryView<String, Integer>, Optional<Integer>> & Serializable)
                                            (v, view) ->
                                                    view.find().map(
                                                            (Function<Integer, Integer> & Serializable)
                                                                    previous -> {
                                                                        if (previous == oldValue) {
                                                                            view.set(v);
                                                                            return previous;
                                                                        } else {
                                                                            return previous;
                                                                        }
                                                                    }));

            CompletableFuture<Optional<Integer>> result =
                    replaceFuture.thenCompose((Function<Optional<Integer>, CompletableFuture<Optional<Integer>>> & Serializable)
                            p -> readWriteMap
                                    .eval("key10",
                                            (Function<EntryView.ReadWriteEntryView<String, Integer>, Optional<Integer>> & Serializable)
                                                    EntryView.ReadWriteEntryView::find));

            assertThat(result.join())
                    .hasValue(100);
        });
    }

    @Test
    public void testReadWriteMapLikeReplace2() {
        withCache("distCache", 3, (Cache<String, Integer> cache) -> {
            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, i));

            FunctionalMapImpl<String, Integer> functionalMap = FunctionalMapImpl.create(cache.getAdvancedCache());
            FunctionalMap.ReadWriteMap<String, Integer> readWriteMap = ReadWriteMapImpl.create(functionalMap);

            int oldValue = 50;

            CompletableFuture<Optional<Integer>> replaceFuture =
                    readWriteMap.eval(
                            "key10",
                            100,
                            (BiFunction<Integer, EntryView.ReadWriteEntryView<String, Integer>, Optional<Integer>> & Serializable)
                                    (v, view) ->
                                            view.find().map((Function<Integer, Integer> & Serializable)
                                                    previous -> {
                                                        if (previous == oldValue) {
                                                            view.set(v);
                                                            return previous;
                                                        } else {
                                                            return previous;
                                                        }
                                                    }));

            CompletableFuture<Optional<Integer>> result =
                    replaceFuture.thenCompose((Function<Optional<Integer>, CompletableFuture<Optional<Integer>>> & Serializable)
                            p -> readWriteMap
                                    .eval("key10",
                                            (Function<EntryView.ReadWriteEntryView<String, Integer>, Optional<Integer>> & Serializable)
                                                    EntryView.ReadWriteEntryView::find));

            assertThat(result.join())
                    .hasValue(10);
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
