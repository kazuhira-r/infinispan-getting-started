package org.littlewings.infinispan.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.RemoteCacheManagerAdmin;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.exceptions.HotRodClientException;
import org.infinispan.commons.marshall.Marshaller;
import org.infinispan.configuration.cache.CacheMode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.littlewings.infinispan.entity.Book;
import org.littlewings.infinispan.entity.Result;

import static org.assertj.core.api.Assertions.*;

public class TaskClientTest {
    List<Book> books =
            List.of(
                    Book.create("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 5344),
                    Book.create("978-1849518222", "Infinispan Data Grid Platform", 3608),
                    Book.create("978-0359439379", "The Apache Ignite Book", 7686),
                    Book.create("978-1365732355", "High Performance in-memory computing with Apache Ignite", 6342),
                    Book.create("978-1789347531", "Apache Ignite Quick Start Guide: Distributed data caching and processing made easy", 3638),
                    Book.create("978-1785285332", "Getting Started with Hazelcast - Second Edition: Get acquainted with the highly scalable data grid, Hazelcast, and learn how to bring its powerful in-memory features into your application", 4209),
                    Book.create("978-1617295522", "Spark in Action, Second Edition: Covers Apache Spark 3 with Examples in Java, Python, and Scala", 6297),
                    Book.create("978-1484257807", "Beginning Apache Spark Using Azure Databricks: Unleashing Large Cluster Analytics in the Cloud", 4817),
                    Book.create("978-1788997829", "Apache Kafka Quick Start Guide: Leverage Apache Kafka 2.0 to simplify real-time data processing for distributed applications", 3516),
                    Book.create("978-1491936160", "Kafka: The Definitive Guide: Real-Time Data and Stream Processing at Scale", 4989)
            );

    <K, V> void withRemoteCache(String cacheName, Consumer<RemoteCache<K, V>> func) {
        Configuration configuration =
                new ConfigurationBuilder()
                        .uri("hotrod://ispn-admin:admin-password@172.17.0.2:11222,172.17.0.3:11222,172.17.0.4:11222"
                                + "?context-initializers=org.littlewings.infinispan.entity.EntitiesInitializerImpl")
                        .build();

        try (RemoteCacheManager manager = new RemoteCacheManager(configuration)) {
            RemoteCache<K, V> cache = manager.getCache(cacheName);

            func.accept(cache);
        }
    }

    @BeforeAll
    static void createCache() {
        Configuration configuration =
                new ConfigurationBuilder()
                        .uri("hotrod://ispn-admin:admin-password@172.17.0.2:11222,172.17.0.3:11222,172.17.0.4:11222")
                        .build();

        try (RemoteCacheManager manager = new RemoteCacheManager(configuration)) {
            RemoteCacheManagerAdmin admin = manager.administration();

            admin.removeCache("bookCache");

            org.infinispan.configuration.cache.Configuration cacheConfiguration =
                    new org.infinispan.configuration.cache.ConfigurationBuilder()
                            .clustering().cacheMode(CacheMode.DIST_SYNC)
                            .security().authorization().enable()
                            .encoding().key().mediaType("application/x-protostream")
                            .encoding().value().mediaType("application/x-protostream")
                            .build();
            admin.getOrCreateCache("bookCache", cacheConfiguration);
        }
    }

    @Test
    public void failMarshallingSynchronizedList() {
        this.<String, Book>withRemoteCache("bookCache", cache -> {
            Marshaller marshaller = cache.getRemoteCacheManager().getMarshaller();

            List<String> list = Collections.synchronizedList(new ArrayList<>());
            list.add("Hello");
            list.add("World");

            assertThatThrownBy(() -> marshaller.objectToByteBuffer(list))
                    .hasMessageContaining("No marshaller registered for object of Java type java.util.Collections$SynchronizedRandomAccessList")
                    .isInstanceOf(IllegalArgumentException.class);
        });
    }

    @Test
    public void marshallingArrayList() {
        this.<String, Book>withRemoteCache("bookCache", cache -> {
            Marshaller marshaller = cache.getRemoteCacheManager().getMarshaller();

            List<String> list = new ArrayList<>();
            list.add("Hello");
            list.add("World");

            try {
                byte[] binary = marshaller.objectToByteBuffer(list);
                assertThat(binary).hasSize(67);

                assertThat((List<String>) marshaller.objectFromByteBuffer(binary))
                        .isEqualTo(list)
                        .isInstanceOf(ArrayList.class)
                        .containsExactly("Hello", "World");
            } catch (IOException | InterruptedException | ClassNotFoundException e) {
                fail("fail", e);
            }
        });
    }

    @Test
    public void singleNodeTask() {
        this.<String, Book>withRemoteCache("bookCache", cache -> {
            books.forEach(b -> cache.put(b.getIsbn(), b));

            Result result = cache.execute("price-sum-task", Collections.emptyMap());

            assertThat(result.getValue()).isEqualTo(50446);

            cache.clear();
            assertThat(cache).isEmpty();
        });
    }

    @Test
    public void singleNodeTaskWithParameters() {
        this.<String, Book>withRemoteCache("bookCache", cache -> {
            books.forEach(b -> cache.put(b.getIsbn(), b));

            Map<String, Object> parameters = Map.of("greaterThanPrice", 5000);

            Result result = cache.execute("price-sum-task", parameters);

            assertThat(result.getValue()).isEqualTo(25669);

            cache.clear();
            assertThat(cache).isEmpty();
        });
    }

    @Test
    public void allNodesTask() {
        this.<String, Book>withRemoteCache("bookCache", cache -> {
            books.forEach(b -> cache.put(b.getIsbn(), b));

            assertThatThrownBy(() -> cache.execute("price-sum-all-nodes-task", Collections.emptyMap()))
                    .isInstanceOf(HotRodClientException.class)
                    .hasMessage("org.infinispan.commons.marshall.MarshallingException: ISPN000615: Unable to unmarshall 'java.util.Collections$SynchronizedRandomAccessList' as a marshaller is not present in the user or global SerializationContext");

            cache.clear();
            assertThat(cache).isEmpty();
        });
    }

    @Test
    public void allNodesTaskWithParameters() {
        this.<String, Book>withRemoteCache("bookCache", cache -> {
            books.forEach(b -> cache.put(b.getIsbn(), b));

            Map<String, Object> parameters = Map.of("greaterThanPrice", 5000);

            assertThatThrownBy(() -> cache.execute("price-sum-all-nodes-task", parameters))
                    .isInstanceOf(HotRodClientException.class)
                    .hasMessage("org.infinispan.commons.marshall.MarshallingException: ISPN000615: Unable to unmarshall 'java.util.Collections$SynchronizedRandomAccessList' as a marshaller is not present in the user or global SerializationContext");

            cache.clear();
            assertThat(cache).isEmpty();
        });
    }
}
