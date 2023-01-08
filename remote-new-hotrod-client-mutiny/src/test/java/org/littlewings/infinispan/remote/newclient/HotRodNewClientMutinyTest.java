package org.littlewings.infinispan.remote.newclient;

import java.net.URI;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.infinispan.api.Infinispan;
import org.infinispan.api.mutiny.MutinyCache;
import org.infinispan.api.mutiny.MutinyContainer;
import org.infinispan.hotrod.configuration.HotRodConfiguration;
import org.infinispan.hotrod.configuration.HotRodConfigurationBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HotRodNewClientMutinyTest {
    @Test
    public void simpleMutinyCache() {
        URI uri = URI.create("hotrod://ispn-user:password@172.18.0.2:11222,172.18.0.3:11222,172.18.0.4:11222");

        try (Infinispan infinispan = Infinispan.create(uri);
             // または
             // try (Infinispan infinispan = Infinispan.create(uriString)) {
             MutinyContainer container = infinispan.mutiny()) {
            Uni<MutinyCache<String, String>> cache =
                    container
                            .caches()
                            .get("simpleCache");

            Multi<Void> setOperation =
                    cache
                            .toMulti()
                            .onItem()
                            .transformToMultiAndMerge(c ->
                                    Multi
                                            .createFrom()
                                            .items(
                                                    IntStream
                                                            .rangeClosed(1, 100)
                                                            .mapToObj(i -> c.set("key" + i, "value" + i))
                                            )
                                            .onItem()
                                            .transformToUniAndMerge(Function.identity())
                            );

            setOperation.collect().asList().await().indefinitely();

            cache
                    .onItem()
                    .transformToUni(c -> c.get("key1"))
                    .subscribe()
                    .withSubscriber(UniAssertSubscriber.create())
                    .awaitItem()
                    .assertItem("value1");
            cache
                    .onItem()
                    .transformToUni(c -> c.get("key50"))
                    .subscribe()
                    .withSubscriber(UniAssertSubscriber.create())
                    .awaitItem()
                    .assertItem("value50");
            cache
                    .onItem()
                    .transformToUni(c -> c.get("key100"))
                    .subscribe()
                    .withSubscriber(UniAssertSubscriber.create())
                    .awaitItem()
                    .assertItem("value100");

            cache.onItem().transformToUni(c -> c.clear()).await().indefinitely();

            cache
                    .onItem()
                    .transformToUni(c -> c.get("key1"))
                    .subscribe()
                    .withSubscriber(UniAssertSubscriber.create())
                    .awaitItem()
                    .assertItem(null);
            cache
                    .onItem()
                    .transformToUni(c -> c.get("key50"))
                    .subscribe()
                    .withSubscriber(UniAssertSubscriber.create())
                    .awaitItem()
                    .assertItem(null);
            cache
                    .onItem()
                    .transformToUni(c -> c.get("key100"))
                    .subscribe()
                    .withSubscriber(UniAssertSubscriber.create())
                    .awaitItem()
                    .assertItem(null);
        }
    }

    @Test
    public void bookMutinyCache() {
        // URIではPropertiesの部分は読まない
        //URI uri = URI.create("hotrod://ispn-user:password@172.18.0.2:11222,172.18.0.3:11222,172.18.0.4:11222?context-initializers=org.littlewings.infinispan.remote.newclient.EntitiesInitializerImpl");

        HotRodConfiguration configuration =
                new HotRodConfigurationBuilder()
                        .addServers("172.18.0.2:11222;172.18.0.3:11222;172.18.0.4:11222")
                        .addContextInitializer("org.littlewings.infinispan.remote.newclient.EntitiesInitializerImpl")
                        .security()
                        .authentication()
                        .username("ispn-user")
                        .password("password".toCharArray())
                        .build();

        try (Infinispan infinispan = Infinispan.create(configuration);
             MutinyContainer container = infinispan.mutiny()) {

            Uni<MutinyCache<String, Book>> cache =
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

            Multi<Void> setOperation =
                    cache
                            .toMulti()
                            .onItem()
                            .transformToMultiAndMerge(c ->
                                    Multi
                                            .createFrom()
                                            .items(
                                                    books
                                                            .stream()
                                                            .map(b -> c.set(b.getIsbn(), b))
                                            )
                                            .onItem()
                                            .transformToUniAndMerge(Function.identity())

                            );

            setOperation.collect().asList().await().indefinitely();

            cache
                    .onItem()
                    .transformToUni(c -> c.get("978-1782169970"))
                    .onItem()
                    .invoke(b -> {
                        assertThat(b.getTitle()).isEqualTo("Infinispan Data Grid Platform Definitive Guide");
                        assertThat(b.getPrice()).isEqualTo(5344);
                    })
                    .subscribe()
                    .withSubscriber(UniAssertSubscriber.create())
                    .awaitItem()
                    .assertCompleted();

            cache
                    .onItem()
                    .transformToUni(c -> c.get("978-0359439379"))
                    .onItem()
                    .invoke(b -> {
                        assertThat(b.getTitle()).isEqualTo("The Apache Ignite Book");
                        assertThat(b.getPrice()).isEqualTo(9964);
                    })
                    .subscribe()
                    .withSubscriber(UniAssertSubscriber.create())
                    .awaitItem()
                    .assertCompleted();

            cache.onItem().transformToUni(c -> c.clear()).await().indefinitely();

            cache
                    .onItem()
                    .transformToUni(c -> c.get("978-1782169970"))
                    .subscribe()
                    .withSubscriber(UniAssertSubscriber.create())
                    .awaitItem()
                    .assertItem(null);

            cache
                    .onItem()
                    .transformToUni(c -> c.get("978-0359439379"))
                    .subscribe()
                    .withSubscriber(UniAssertSubscriber.create())
                    .awaitItem()
                    .assertItem(null);
        }
    }
}
