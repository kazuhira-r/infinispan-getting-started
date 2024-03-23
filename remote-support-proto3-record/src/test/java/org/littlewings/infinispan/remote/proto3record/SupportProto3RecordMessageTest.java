package org.littlewings.infinispan.remote.proto3record;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.RemoteCacheManagerAdmin;
import org.infinispan.commons.api.query.Query;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.IndexStartupMode;
import org.infinispan.configuration.cache.IndexStorage;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class SupportProto3RecordMessageTest {
    String createUri(String userName, String password) {
        return String.format(
                "hotrod://%s:%s@172.18.0.2:11222,172.18.0.3:11222,172.18.0.4:11222"
                        + "?context-initializers=org.littlewings.infinispan.remote.proto3record.EntitiesInitializerImpl",
                userName,
                password
        );
    }

    @BeforeEach
    void setUp() {
        String uri = createUri("ispn-admin", "password");

        try (RemoteCacheManager manager = new RemoteCacheManager(uri)) {
            manager.getConfiguration().getContextInitializers().forEach(serializationContextInitializer -> {
                RemoteCache<String, String> protoCache = manager.getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME);
                protoCache.put("entities.proto", serializationContextInitializer.getProtoFile());
            });

            RemoteCacheManagerAdmin admin = manager.administration();

            // インデックスなしのDistributed Cache
            org.infinispan.configuration.cache.Configuration indexLessDistCacheConfiguration =
                    new org.infinispan.configuration.cache.ConfigurationBuilder()
                            .clustering()
                            .cacheMode(org.infinispan.configuration.cache.CacheMode.DIST_SYNC)
                            .encoding().key().mediaType("application/x-protostream")
                            .encoding().value().mediaType("application/x-protostream")
                            .build();

            // キャッシュがない場合は作成、すでにある場合はデータを削除
            admin.getOrCreateCache("bookCache", indexLessDistCacheConfiguration)
                    .clear();

            // インデックスありのDistributed Cache
            org.infinispan.configuration.cache.Configuration indexedDistCacheConfiguration =
                    new org.infinispan.configuration.cache.ConfigurationBuilder()
                            .clustering()
                            .cacheMode(CacheMode.DIST_SYNC)
                            .encoding().key().mediaType("application/x-protostream")
                            .encoding().value().mediaType("application/x-protostream")
                            // indexing
                            .indexing()
                            .enable()
                            .addIndexedEntities("entity.IndexedBook")
                            .storage(IndexStorage.FILESYSTEM)
                            .path("${infinispan.server.data.path}/index/indexedBookCache")
                            .startupMode(IndexStartupMode.REINDEX)
                            .reader().refreshInterval(0L)  // default 0
                            .writer().commitInterval(1000)  // default null
                            .build();

            // キャッシュがない場合は作成、すでにある場合はデータを削除
            admin.getOrCreateCache("indexedBookCache", indexedDistCacheConfiguration)
                    .clear();
        }
    }

    <K, V> void withCache(String cacheName, Consumer<RemoteCache<K, V>> func) {
        String uri = createUri("ispn-user", "password");

        try (RemoteCacheManager manager = new RemoteCacheManager(uri)) {
            RemoteCache<K, V> cache = manager.getCache(cacheName);

            func.accept(cache);
        }
    }

    @Test
    void records() {
        this.<String, Book>withCache("bookCache", cache -> {
            Book infinispanBook = new Book("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 5242);
            Book redisBook = new Book("978-4798045733", "RDB技術者のためのNoSQLガイド", 3400);
            Book hazelcastBook = new Book("978-1785285332", "Getting Started with Hazelcast - Second Edition", 4129);
            Book igniteBook = new Book("978-1789347531", "Apache Ignite Quick Start Guide: Distributed data caching and processing made easy", 3476);

            cache.put(infinispanBook.isbn(), infinispanBook);
            cache.put(redisBook.isbn(), redisBook);
            cache.put(hazelcastBook.isbn(), hazelcastBook);
            cache.put(igniteBook.isbn(), igniteBook);

            assertThat(cache.size()).isEqualTo(4);

            assertThat(cache.get(infinispanBook.isbn())).isEqualTo(infinispanBook);
            assertThat(cache.get(redisBook.isbn())).isEqualTo(redisBook);
            assertThat(cache.get(hazelcastBook.isbn())).isEqualTo(hazelcastBook);
            assertThat(cache.get(igniteBook.isbn())).isEqualTo(igniteBook);

            cache.clear();
            assertThat(cache.size()).isEqualTo(0);
        });
    }

    @Test
    void indexLessQuery() {
        this.<String, Book>withCache("bookCache", cache -> {
            List<Book> books = List.of(
                    new Book("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 5242),
                    new Book("978-4798045733", "RDB技術者のためのNoSQLガイド", 3400),
                    new Book("978-1785285332", "Getting Started with Hazelcast - Second Edition", 4129),
                    new Book("978-1789347531", "Apache Ignite Quick Start Guide: Distributed data caching and processing made easy", 3476)
            );

            books.forEach(book -> cache.put(book.isbn(), book));

            assertThat(cache.size()).isEqualTo(4);

            Query<Book> query =
                    cache.query("from entity.Book where price > :price order by price desc");
            query.setParameter("price", 4000);

            List<Book> results = query.execute().list();

            assertThat(results).hasSize(2);
            assertThat(results.get(0).title()).isEqualTo("Infinispan Data Grid Platform Definitive Guide");
            assertThat(results.get(1).title()).isEqualTo("Getting Started with Hazelcast - Second Edition");
        });
    }

    @Test
    void indexedQuery() {
        this.<String, IndexedBook>withCache("indexedBookCache", cache -> {
            List<IndexedBook> books = List.of(
                    new IndexedBook("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 5242),
                    new IndexedBook("978-4798045733", "RDB技術者のためのNoSQLガイド", 3400),
                    new IndexedBook("978-1785285332", "Getting Started with Hazelcast - Second Edition", 4129),
                    new IndexedBook("978-1789347531", "Apache Ignite Quick Start Guide: Distributed data caching and processing made easy", 3476)
            );

            books.forEach(book -> cache.put(book.isbn(), book));

            assertThat(cache.size()).isEqualTo(4);

            Query<IndexedBook> query1 =
                    cache.query("from entity.IndexedBook where title: 'guide' and price: [4000 to *] order by price");

            List<IndexedBook> results1 = query1.execute().list();

            assertThat(results1).hasSize(1);
            assertThat(results1.get(0).title()).isEqualTo("Infinispan Data Grid Platform Definitive Guide");

            Query<IndexedBook> query2 =
                    cache.query("from entity.IndexedBook where isbn = '978-4798045733'");

            List<IndexedBook> results2 = query2.execute().list();

            assertThat(results2).hasSize(1);
            assertThat(results2.get(0).title()).isEqualTo("RDB技術者のためのNoSQLガイド");

            Query<IndexedBook> query3 =
                    cache.query("from entity.IndexedBook where (title: 'data' and title: 'guide') or price: [* to 3400] order by price desc, isbn asc");

            List<IndexedBook> results3 = query3.execute().list();

            assertThat(results3).hasSize(3);
            assertThat(results3.get(0).title()).isEqualTo("Infinispan Data Grid Platform Definitive Guide");
            assertThat(results3.get(1).title()).isEqualTo("Apache Ignite Quick Start Guide: Distributed data caching and processing made easy");
            assertThat(results3.get(2).title()).isEqualTo("RDB技術者のためのNoSQLガイド");
        });
    }
}
