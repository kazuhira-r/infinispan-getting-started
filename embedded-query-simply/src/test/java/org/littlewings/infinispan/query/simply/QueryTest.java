package org.littlewings.infinispan.query.simply;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.query.Indexer;
import org.infinispan.query.Search;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryTest {
    <K, V> void withCache(String cacheName, int numInstances, Consumer<Cache<K, V>> func) {
        List<EmbeddedCacheManager> managers =
                IntStream
                        .rangeClosed(1, numInstances)
                        .mapToObj(i -> {
                            try {
                                System.setProperty("node.index", Integer.toString(i));
                                System.setProperty("node.start.timestamp", Long.toString(System.currentTimeMillis()));
                                return new DefaultCacheManager("infinispan.xml");
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        })
                        .collect(Collectors.toList());

        managers.forEach(m -> m.getCache(cacheName));

        try {
            Cache<K, V> cache = managers.get(0).getCache(cacheName);

            func.accept(cache);
        } finally {
            managers.forEach(m -> m.stop());
        }
    }

    @Test
    public void nonIndexingSearch() {
        this.<String, Book>withCache("bookCache", 3, cache -> {
            List<Book> books = List.of(
                    Book.create("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 5242),
                    Book.create("978-4048917353", "Redis入門 インメモリKVSによる高速データ管理", 3400),
                    Book.create("978-4798045733", "RDB技術者のためのNoSQLガイド", 3400),
                    Book.create("978-1785285332", "Getting Started with Hazelcast - Second Edition", 4129),
                    Book.create("978-1789347531", "Apache Ignite Quick Start Guide: Distributed data caching and processing made easy", 3476)
            );

            books.forEach(book -> cache.put(book.getIsbn(), book));

            QueryFactory qf = Search.getQueryFactory(cache);

            Query<Book> query =
                    qf.create("from " + Book.class.getName()
                            + " where price > :price order by price desc");
            query.setParameter("price", 4000);

            List<Book> results = query.execute().list();

            assertThat(results).hasSize(2);
            assertThat(results.get(0).getTitle()).isEqualTo("Infinispan Data Grid Platform Definitive Guide");
            assertThat(results.get(1).getTitle()).isEqualTo("Getting Started with Hazelcast - Second Edition");
        });
    }

    @Test
    public void indexingSearch1() {
        this.<String, IndexedBook>withCache("indexedBookCache", 3, cache -> {
            List<IndexedBook> books = List.of(
                    IndexedBook.create("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 5242),
                    IndexedBook.create("978-4048917353", "Redis入門 インメモリKVSによる高速データ管理", 3400),
                    IndexedBook.create("978-4798045733", "RDB技術者のためのNoSQLガイド", 3400),
                    IndexedBook.create("978-1785285332", "Getting Started with Hazelcast - Second Edition", 4129),
                    IndexedBook.create("978-1789347531", "Apache Ignite Quick Start Guide: Distributed data caching and processing made easy", 3476)
            );

            books.forEach(book -> cache.put(book.getIsbn(), book));

            QueryFactory qf = Search.getQueryFactory(cache);

            Query<IndexedBook> query =
                    qf.create("from " + IndexedBook.class.getName()
                            + " where title: 'guide' and price: [4000 to *] order by price desc");

            List<IndexedBook> results = query.execute().list();

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getTitle()).isEqualTo("Infinispan Data Grid Platform Definitive Guide");
        });
    }

    @Test
    public void indexingSearch2() {
        this.<String, IndexedBook>withCache("indexedBookCache", 3, cache -> {
            List<IndexedBook> books = List.of(
                    IndexedBook.create("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 5242),
                    IndexedBook.create("978-4048917353", "Redis入門 インメモリKVSによる高速データ管理", 3400),
                    IndexedBook.create("978-4798045733", "RDB技術者のためのNoSQLガイド", 3400),
                    IndexedBook.create("978-1785285332", "Getting Started with Hazelcast - Second Edition", 4129),
                    IndexedBook.create("978-1789347531", "Apache Ignite Quick Start Guide: Distributed data caching and processing made easy", 3476)
            );

            books.forEach(book -> cache.put(book.getIsbn(), book));

            QueryFactory qf = Search.getQueryFactory(cache);

            Query<IndexedBook> badQuery =
                    qf.create("from " + IndexedBook.class.getName()
                            + " where title: 'メモ' and price: [* to 3500] order by price desc");

            List<IndexedBook> badResults = badQuery.execute().list();
            assertThat(badResults).isEmpty();

            Query<IndexedBook> query =
                    qf.create("from " + IndexedBook.class.getName()
                            + " where title: 'メモリ' and price: [* to 3500] order by price desc");

            List<IndexedBook> results = query.execute().list();

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getTitle()).isEqualTo("Redis入門 インメモリKVSによる高速データ管理");
        });
    }

    @Test
    public void rebuildIndex() {
        this.<String, IndexedBook>withCache("indexedBookCache", 3, cache -> {
            List<IndexedBook> books = List.of(
                    IndexedBook.create("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 5242),
                    IndexedBook.create("978-4048917353", "Redis入門 インメモリKVSによる高速データ管理", 3400),
                    IndexedBook.create("978-4798045733", "RDB技術者のためのNoSQLガイド", 3400),
                    IndexedBook.create("978-1785285332", "Getting Started with Hazelcast - Second Edition", 4129),
                    IndexedBook.create("978-1789347531", "Apache Ignite Quick Start Guide: Distributed data caching and processing made easy", 3476)
            );

            books.forEach(book -> cache.put(book.getIsbn(), book));

            Indexer indexer = Search.getIndexer(cache);

            CompletionStage<Void> stage = indexer.run();
            stage.toCompletableFuture().join();

            assertThat(indexer.isRunning()).isFalse();
        });
    }
}
