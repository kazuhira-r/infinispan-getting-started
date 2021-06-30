package org.littlewings.infinispan.distexec.protostream;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.junit.jupiter.api.Test;
import org.littlewings.infinispan.distexec.protostream.entity.ProtoBook;
import org.littlewings.infinispan.distexec.protostream.entity.ProtoSummary;
import org.littlewings.infinispan.distexec.protostream.entity.SerializableSummary;

import static org.assertj.core.api.Assertions.assertThat;

public class ProtoStreamDistExecSimplyTest {
    List<ProtoBook> books =
            List.of(
                    ProtoBook.create("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 5344),
                    ProtoBook.create("978-1849518222", "Infinispan Data Grid Platform", 3608),
                    ProtoBook.create("978-0359439379", "The Apache Ignite Book", 7686),
                    ProtoBook.create("978-1365732355", "High Performance in-memory computing with Apache Ignite", 6342),
                    ProtoBook.create("978-1789347531", "Apache Ignite Quick Start Guide: Distributed data caching and processing made easy", 3638),
                    ProtoBook.create("978-1785285332", "Getting Started with Hazelcast - Second Edition: Get acquainted with the highly scalable data grid, Hazelcast, and learn how to bring its powerful in-memory features into your application", 4209),
                    ProtoBook.create("978-1617295522", "Spark in Action, Second Edition: Covers Apache Spark 3 with Examples in Java, Python, and Scala", 6297),
                    ProtoBook.create("978-1484257807", "Beginning Apache Spark Using Azure Databricks: Unleashing Large Cluster Analytics in the Cloud", 4817),
                    ProtoBook.create("978-1788997829", "Apache Kafka Quick Start Guide: Leverage Apache Kafka 2.0 to simplify real-time data processing for distributed applications", 3516),
                    ProtoBook.create("978-1491936160", "Kafka: The Definitive Guide: Real-Time Data and Stream Processing at Scale", 4989)
            );

    <K, V> void withCache(String configurationXmlPath, String cacheName, int numInstances, Consumer<Cache<K, V>> func) {
        List<EmbeddedCacheManager> managers =
                IntStream
                        .rangeClosed(1, numInstances)
                        .mapToObj(i -> {
                            try {
                                return new DefaultCacheManager(configurationXmlPath);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        })
                        .collect(Collectors.toList());

        managers.forEach(manager -> manager.getCache(cacheName));

        try {
            Cache<K, V> cache = managers.get(0).getCache(cacheName);

            func.accept(cache);
        } finally {
            managers.forEach(manager -> manager.stop());
        }
    }

    @Test
    public void returnOnlySerializable() {
        this.<String, ProtoBook>withCache("infinispan-serializable.xml", "bookCache", 3, cache -> {
            books.forEach(book -> cache.put(book.getIsbn(), book));

            StreamSummaryReturnOnlySerializableTask summaryTask = new StreamSummaryReturnOnlySerializableTask(cache);

            SerializableSummary totalPriceSummary = summaryTask.execute();
            assertThat(totalPriceSummary.getValue()).isEqualTo(50446);

            SerializableSummary filteredPriceSummary = summaryTask.execute(5000);
            assertThat(filteredPriceSummary.getValue()).isEqualTo(25669);
        });
    }

    @Test
    public void returnOnlyProto() {
        this.<String, ProtoBook>withCache("infinispan-proto.xml", "bookCache", 3, cache -> {
            books.forEach(book -> cache.put(book.getIsbn(), book));

            StreamSummaryReturnOnlyProtoTask summaryTask = new StreamSummaryReturnOnlyProtoTask(cache);

            ProtoSummary totalPriceSummary = summaryTask.execute();
            assertThat(totalPriceSummary.getValue()).isEqualTo(50446);

            ProtoSummary filteredPriceSummary = summaryTask.execute(5000);
            assertThat(filteredPriceSummary.getValue()).isEqualTo(25669);
        });
    }
}
