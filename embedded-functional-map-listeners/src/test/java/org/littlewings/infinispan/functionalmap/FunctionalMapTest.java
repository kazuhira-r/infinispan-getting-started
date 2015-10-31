package org.littlewings.infinispan.functionalmap;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.infinispan.Cache;
import org.infinispan.commons.api.functional.EntryView;
import org.infinispan.commons.api.functional.FunctionalMap;
import org.infinispan.commons.api.functional.Listeners;
import org.infinispan.functional.impl.FunctionalMapImpl;
import org.infinispan.functional.impl.ReadWriteMapImpl;
import org.infinispan.functional.impl.WriteOnlyMapImpl;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Verifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class FunctionalMapTest {
    private int closeableHandlerCalledCount = 0;
    private int expectedCloseableHandlerCalledCount = 0;
    private int listenerCalledCount = 0;
    private int expectedListenerCalledCount = 0;

    @Rule
    public VerifierRule verifierRule = new VerifierRule();

    private class VerifierRule extends Verifier {
        @Override
        protected void verify() {
            assertThat(closeableHandlerCalledCount)
                    .isEqualTo(expectedCloseableHandlerCalledCount);
            assertThat(listenerCalledCount)
                    .isEqualTo(expectedListenerCalledCount);
        }
    }

    @Test
    public void testWriteListener() {
        withCache("distCache", 1, (Cache<String, Integer> cache) -> {
            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, i));

            FunctionalMapImpl<String, Integer> functionalMap = FunctionalMapImpl.create(cache.getAdvancedCache());
            FunctionalMap.WriteOnlyMap<String, Integer> writeOnlyMap = WriteOnlyMapImpl.create(functionalMap);

            expectedCloseableHandlerCalledCount = 1;
            expectedListenerCalledCount = 1;

            AutoCloseable writeFunctionHandler = writeOnlyMap.listeners().onWrite(written -> {
                closeableHandlerCalledCount++;
                assertThat(written.find())
                        .hasValue(100);
            });

            AutoCloseable writeListenerHandler = writeOnlyMap.listeners().add(new Listeners.WriteListeners.WriteListener<String, Integer>() {
                @Override
                public void onWrite(EntryView.ReadEntryView<String, Integer> write) {
                    listenerCalledCount++;
                    assertThat(write.find()).hasValue(100);
                }
            });

            // onWrite
            writeOnlyMap.eval("key10",
                    100,
                    (BiConsumer<Integer, EntryView.WriteEntryView<Integer>> & Serializable)
                            (v, view) -> view.set(v)).join();

            try {
                writeFunctionHandler.close();
                writeListenerHandler.close();
            } catch (Exception e) {
                fail(e.getMessage());
            }
        });
    }

    @Test
    public void testReadWriteListener() {
        withCache("distCache", 1, (Cache<String, Integer> cache) -> {
            IntStream.rangeClosed(1, 10).forEach(i -> cache.put("key" + i, i));

            FunctionalMapImpl<String, Integer> functionalMap = FunctionalMapImpl.create(cache.getAdvancedCache());
            FunctionalMap.ReadWriteMap<String, Integer> readWriteMap = ReadWriteMapImpl.create(functionalMap);

            List<AutoCloseable> closeableHandlers = new ArrayList<>();

            expectedCloseableHandlerCalledCount = 6;
            expectedListenerCalledCount = 6;

            closeableHandlers.add(readWriteMap.listeners().onCreate(created -> {
                closeableHandlerCalledCount++;
                assertThat(created.find())
                        .hasValue(20);
            }));

            closeableHandlers.add(readWriteMap.listeners().onModify((before, after) -> {
                closeableHandlerCalledCount++;
                assertThat(before.find())
                        .hasValue(10);
                assertThat(after.find())
                        .hasValue(100);
            }));

            closeableHandlers.add(readWriteMap.listeners().onRemove(removed -> {
                closeableHandlerCalledCount++;
                assertThat(removed.find())
                        .hasValue(5);
            }));

            closeableHandlers.add(readWriteMap.listeners().onWrite(written -> {
                closeableHandlerCalledCount++;
                written.find().ifPresent(v -> assertThat(v == 20 || v == 100).isTrue());

                if (!written.find().isPresent()) {
                    assertThat(written.key())
                            .isEqualTo("key5");
                }
            }));

            closeableHandlers.add(readWriteMap.listeners().add(new Listeners.ReadWriteListeners.ReadWriteListener<String, Integer>() {
                @Override
                public void onCreate(EntryView.ReadEntryView<String, Integer> created) {
                    listenerCalledCount++;
                    assertThat(created.find())
                            .hasValue(20);
                }

                @Override
                public void onModify(EntryView.ReadEntryView<String, Integer> before, EntryView.ReadEntryView<String, Integer> after) {
                    listenerCalledCount++;
                    assertThat(before.find())
                            .hasValue(10);
                    assertThat(after.find())
                            .hasValue(100);
                }

                @Override
                public void onRemove(EntryView.ReadEntryView<String, Integer> removed) {
                    listenerCalledCount++;
                    assertThat(removed.find())
                            .hasValue(5);
                }
            }));

            // Listeners.WriteListeners.WriteListener
            closeableHandlers.add(readWriteMap.listeners().add(written -> {
                listenerCalledCount++;
                written.find().ifPresent(v -> {
                    assertThat(v == 20 || v == 100)
                            .isTrue();
                });

                if (!written.find().isPresent()) {
                    assertThat(written.key())
                            .isEqualTo("key5");
                }
            }));

            // onCreate/onWrite
            readWriteMap.eval("key20",
                    20,
                    (BiFunction<Integer, EntryView.ReadWriteEntryView<String, Integer>, Void> & Serializable)
                            (v, view) -> view.set(v)).join();
            // onModify/onWrite
            readWriteMap.eval("key10",
                    100,
                    (BiFunction<Integer, EntryView.ReadWriteEntryView<String, Integer>, Void> & Serializable)
                            (v, view) -> view.set(v)).join();
            // onRemove/onWrite
            readWriteMap.eval("key5",
                    (Function<EntryView.ReadWriteEntryView<String, Integer>, Void> & Serializable)
                            view -> view.remove()).join();

            closeableHandlers.forEach(closeable -> {
                try {
                    closeable.close();
                } catch (Exception e) {
                    fail(e.getMessage());
                }
            });
        });
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

        managers.forEach(m -> m.getCache(cacheName));

        try {
            consumer.accept(managers.get(0).getCache(cacheName));
        } finally {
            managers.forEach(EmbeddedCacheManager::stop);
        }
    }
}
