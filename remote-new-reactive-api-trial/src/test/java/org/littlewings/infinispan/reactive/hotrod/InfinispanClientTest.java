package org.littlewings.infinispan.reactive.hotrod;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;
import org.assertj.core.data.MapEntry;
import org.infinispan.api.Infinispan;
import org.infinispan.api.configuration.ClientConfig;
import org.infinispan.api.reactive.KeyValueStore;
import org.infinispan.api.reactive.KeyValueStoreConfig;
import org.infinispan.api.reactive.WriteResult;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InfinispanClientTest {
    @BeforeEach
    public void setUp() {
        Configuration configuration =
                new ConfigurationBuilder()
                        .addServers("172.17.0.2:11222")
                        .build();
        RemoteCacheManager manager = new RemoteCacheManager(configuration);

        manager
                .administration()
                .getOrCreateCache(
                        "gettingStartedCache",
                        new org.infinispan.configuration.cache.ConfigurationBuilder().build()
                )
                .clear();

        manager
                .administration()
                .getOrCreateCache(
                        "bulkCache",
                        new org.infinispan.configuration.cache.ConfigurationBuilder().build()
                )
                .clear();

        manager.stop();
    }

    @Test
    public void gettingStarted() {
        Configuration configuration =
                new ConfigurationBuilder()
                        .addServers("172.17.0.2:11222")
                        .build();

        ClientConfig config = ClientConfig.from(configuration.properties());
        Infinispan infinispan = Infinispan.newClient(config);

        KeyValueStore<String, String> store =
                infinispan
                        .<String, String>getKeyValueStore("gettingStartedCache", KeyValueStoreConfig.defaultConfig())
                        .toCompletableFuture()
                        .join();

        CompletionStage<String> value = store
                .insert("key1", "value1")
                .thenCompose(b -> store.get("key1"));

        assertThat(value.toCompletableFuture().join()).isEqualTo("value1");

        CompletionStage<Long> estimateSize =
                store
                        .insert("key2", "value2")
                        .thenCompose(b -> store.insert("key3", "value3"))
                        .thenCompose(b -> store.estimateSize());

        assertThat(estimateSize.toCompletableFuture().join()).isEqualTo(3);

        CompletionStage<String> deletedEntry =
                store
                        .delete("key1")
                        .thenCompose(v -> store.get("key1"));

        assertThat(deletedEntry.toCompletableFuture().join()).isNull();

        CompletionStage<String> value3 = store
                .insert("key3", "value3-1")
                .thenCompose(b -> store.get("key3"));

        assertThat(value3.toCompletableFuture().join()).isEqualTo("value3");

        infinispan.stop().toCompletableFuture().join();
    }

    @Test
    public void bulkOperation() {
        Configuration configuration =
                new ConfigurationBuilder()
                        .addServers("172.17.0.2:11222")
                        .build();

        ClientConfig config = ClientConfig.from(configuration.properties());
        Infinispan infinispan = Infinispan.newClient(config);

        KeyValueStore<String, String> store =
                infinispan
                        .<String, String>getKeyValueStore("bulkCache", KeyValueStoreConfig.defaultConfig())
                        .toCompletableFuture()
                        .join();

        ///// put many
        Flowable<WriteResult<String>> writeResult =
                Flowable
                        .fromPublisher(
                                store.saveMany(Flowable.fromIterable(Map.of("key1", "value1", "key2", "value2").entrySet()))
                        );

        TestSubscriber<Map.Entry<String, Boolean>> writeResultSubscriber = TestSubscriber.create();
        writeResult.map(ws -> MapEntry.entry(ws.getKey(), ws.isError())).subscribe(writeResultSubscriber);

        writeResultSubscriber
                .assertValueSetOnly(
                        Set.of(
                                MapEntry.<String, Boolean>entry("key1", false),
                                MapEntry.<String, Boolean>entry("key2", false)
                        )
                );

        ///// keys
        Flowable<String> keys =
                Flowable.fromPublisher(store.keys());

        TestSubscriber<String> keysSubscriber = TestSubscriber.create();
        keys.subscribe(keysSubscriber);
        keysSubscriber.assertValues("key1", "key2");

        ///// entries
        Flowable<Map.Entry<String, String>> entries =
                Flowable.fromPublisher(store.entries());

        TestSubscriber<Map.Entry<String, String>> entriesSubscriber = TestSubscriber.create();
        entries.subscribe(entriesSubscriber);
        entriesSubscriber.assertValues(MapEntry.entry("key1", "value1"), MapEntry.entry("key2", "value2"));

        infinispan.stop().toCompletableFuture().join();
    }
}
