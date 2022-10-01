package org.littlewings.infinispan.remote.newclient;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.IntStream;

import org.infinispan.api.Infinispan;
import org.infinispan.api.async.AsyncCache;
import org.infinispan.api.async.AsyncContainer;
import org.infinispan.api.mutiny.MutinyContainer;
import org.infinispan.api.sync.SyncCache;
import org.infinispan.api.sync.SyncContainer;
import org.infinispan.api.sync.events.cache.SyncCacheContinuousQueryListener;
import org.infinispan.hotrod.configuration.HotRodConfiguration;
import org.infinispan.hotrod.configuration.HotRodConfigurationBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class HotRodNewClientTest {
    @Test
    public void connectInfinispanServerUsingURI() {
        String uriString = "hotrod://ispn-user:password@172.17.0.2:11222";
        URI uri = URI.create(uriString);

        try (Infinispan infinispan = Infinispan.create(uri);
             // または
             // try (Infinispan infinispan = Infinispan.create(uriString)) {
             SyncContainer container = infinispan.sync()) {
            SyncCache<String, String> cache =
                    container
                            .caches()
                            .get("simpleCache");

            cache.clear();
        }
    }

    @Test
    public void connectInfinispanServerUsingConfiguration() {
        HotRodConfiguration configuration =
                new HotRodConfigurationBuilder()
                        .addServers("172.17.0.2:11222")
                        .security()
                        .authentication()
                        .username("ispn-user")
                        .password("password".toCharArray())
                        .build();

        try (Infinispan infinispan = Infinispan.create(configuration);
             SyncContainer container = infinispan.sync()) {
            SyncCache<String, String> cache =
                    container
                            .caches()
                            .get("simpleCache");

            cache.clear();
        }
    }

    @Test
    public void simpleSyncCache() {
        URI uri = URI.create("hotrod://ispn-user:password@172.17.0.2:11222");

        try (Infinispan infinispan = Infinispan.create(uri);
             SyncContainer container = infinispan.sync()) {
            SyncCache<String, String> cache =
                    container
                            .caches()
                            .get("simpleCache");

            IntStream
                    .rangeClosed(1, 100)
                    .forEach(i -> cache.set("key" + i, "value" + i));

            assertThat(cache.get("key1")).isEqualTo("value1");
            assertThat(cache.get("key50")).isEqualTo("value50");
            assertThat(cache.get("key100")).isEqualTo("value100");

            cache.clear();

            assertThat(cache.get("key1")).isNull();
            assertThat(cache.get("key50")).isNull();
            assertThat(cache.get("key100")).isNull();
        }
    }

    @Test
    public void simpleSyncCacheUnsupportedOperation() {
        URI uri = URI.create("hotrod://ispn-user:password@172.17.0.2:11222");

        try (Infinispan infinispan = Infinispan.create(uri);
             SyncContainer container = infinispan.sync()) {
            SyncCache<String, String> cache =
                    container
                            .caches()
                            .get("simpleCache");

            assertThatThrownBy(() -> cache.entries())
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> cache.keys())
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> cache.listen(new SyncCacheContinuousQueryListener<>() {
            }))
                    .isInstanceOf(UnsupportedOperationException.class);

            assertThatThrownBy(() -> cache.estimateSize())
                    .isInstanceOf(UnsupportedOperationException.class);

        }
    }

    @Test
    public void bookSyncCache() {
        // URIではPropertiesの部分は読まない
        //URI uri = URI.create("hotrod://ispn-user:password@172.17.0.2:11222?context-initializers=org.littlewings.infinispan.remote.newclient.EntitiesInitializerImpl");

        HotRodConfiguration configuration =
                new HotRodConfigurationBuilder()
                        .addServers("172.17.0.2:11222")
                        .addContextInitializer("org.littlewings.infinispan.remote.newclient.EntitiesInitializerImpl")
                        .security()
                        .authentication()
                        .username("ispn-user")
                        .password("password".toCharArray())
                        .build();

        try (Infinispan infinispan = Infinispan.create(configuration);
             SyncContainer container = infinispan.sync()) {
            SyncCache<String, Book> cache =
                    container
                            .caches()
                            .get("bookCache");

            List<Book> books = List.of(
                    Book.create("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 5344),
                    Book.create("978-1785285332", "Getting Started with Hazelcast - Second Edition", 5484),
                    Book.create("978-0359439379", "The Apache Ignite Book", 9964),
                    Book.create("978-1783988181", "Mastering Redis", 8719),
                    Book.create("978-1492080510", "High Performance MySQL", 6428)
            );

            books.forEach(b -> cache.set(b.getIsbn(), b));

            assertThat(cache.get("978-1782169970").getTitle())
                    .isEqualTo("Infinispan Data Grid Platform Definitive Guide");
            assertThat(cache.get("978-1782169970").getPrice())
                    .isEqualTo(5344);
            assertThat(cache.get("978-0359439379").getTitle())
                    .isEqualTo("The Apache Ignite Book");
            assertThat(cache.get("978-0359439379").getPrice())
                    .isEqualTo(9964);

            cache.clear();

            assertThat(cache.get("978-1782169970")).isNull();
            assertThat(cache.get("978-1782169970")).isNull();
        }
    }

    @Test
    public void simpleAsyncCache() {
        URI uri = URI.create("hotrod://ispn-user:password@172.17.0.2:11222");

        try (Infinispan infinispan = Infinispan.create(uri);
             AsyncContainer container = infinispan.async()) {
            AsyncCache<String, String> cache =
                    container
                            .caches()
                            .<String, String>get("simpleCache")
                            .toCompletableFuture()
                            .join();

            IntStream
                    .rangeClosed(1, 100)
                    .<CompletionStage<?>>mapToObj(i -> cache.set("key" + i, "value" + i))
                    .map(CompletionStage::toCompletableFuture)
                    .forEach(CompletableFuture::join);

            cache
                    .get("key1")
                    .thenAccept(value -> assertThat(value).isEqualTo("value1"))
                    .thenCompose(v -> cache.get("key50"))
                    .thenAccept(value -> assertThat(value).isEqualTo("value50"))
                    .thenCompose(v -> cache.get("key100"))
                    .thenAccept(value -> assertThat(value).isEqualTo("value100"))
                    .toCompletableFuture()
                    .join();

            cache.clear().toCompletableFuture().join();

            cache
                    .get("key1")
                    .thenAccept(value -> assertThat(value).isNull())
                    .thenCompose(v -> cache.get("key50"))
                    .thenAccept(value -> assertThat(value).isNull())
                    .thenCompose(v -> cache.get("key100"))
                    .thenAccept(value -> assertThat(value).isNull())
                    .toCompletableFuture()
                    .join();
        }
    }

    @Test
    public void bookAsyncCache() {
        HotRodConfiguration configuration =
                new HotRodConfigurationBuilder()
                        .addServers("172.17.0.2:11222")
                        .addContextInitializer("org.littlewings.infinispan.remote.newclient.EntitiesInitializerImpl")
                        .security()
                        .authentication()
                        .username("ispn-user")
                        .password("password".toCharArray())
                        .build();

        try (Infinispan infinispan = Infinispan.create(configuration);
             AsyncContainer container = infinispan.async()) {
            AsyncCache<String, Book> cache =
                    container
                            .caches()
                            .<String, Book>get("bookCache")
                            .toCompletableFuture()
                            .join();

            List<Book> books = List.of(
                    Book.create("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 5344),
                    Book.create("978-1785285332", "Getting Started with Hazelcast - Second Edition", 5484),
                    Book.create("978-0359439379", "The Apache Ignite Book", 9964),
                    Book.create("978-1783988181", "Mastering Redis", 8719),
                    Book.create("978-1492080510", "High Performance MySQL", 6428)
            );

            books
                    .stream()
                    .map(b -> cache.set(b.getIsbn(), b).toCompletableFuture())
                    .forEach(CompletableFuture::join);

            cache
                    .get("978-1782169970")
                    .thenAccept(b -> {
                        assertThat(b.getTitle()).isEqualTo("Infinispan Data Grid Platform Definitive Guide");
                        assertThat(b.getPrice()).isEqualTo(5344);
                    })
                    .thenCompose(v -> cache.get("978-0359439379"))
                    .thenAccept(b -> {
                        assertThat(b.getTitle()).isEqualTo("The Apache Ignite Book");
                        assertThat(b.getPrice()).isEqualTo(9964);
                    })
                    .toCompletableFuture()
                    .join();

            cache.clear().toCompletableFuture().join();

            cache
                    .get("978-1782169970")
                    .thenAccept(b -> assertThat(b).isNull())
                    .thenCompose(v -> cache.get("978-0359439379"))
                    .thenAccept(b -> assertThat(b).isNull())
                    .toCompletableFuture()
                    .join();
        }
    }

    @Test
    public void simpleMutinyCache() {
        URI uri = URI.create("hotrod://ispn-user:password@172.17.0.2:11222");

        try (Infinispan infinispan = Infinispan.create(uri);
             MutinyContainer container = infinispan.mutiny()) {
            assertThatThrownBy(() ->
                    container
                            .caches()
                            .<String, String>get("simpleCache")
                            .await()
                            .indefinitely()
            )
                    .isInstanceOf(ClassCastException.class)
                    .hasMessage("class java.util.concurrent.CompletableFuture cannot be cast to class org.infinispan.hotrod.impl.cache.RemoteCache (java.util.concurrent.CompletableFuture is in module java.base of loader 'bootstrap'; org.infinispan.hotrod.impl.cache.RemoteCache is in unnamed module of loader 'app')");
        }
    }
}
