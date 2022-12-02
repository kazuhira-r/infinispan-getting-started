package org.littlewings.infinispan.remote.query;

import java.util.List;
import java.util.function.Consumer;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.RemoteCacheManagerAdmin;
import org.infinispan.client.hotrod.Search;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.IndexStartupMode;
import org.infinispan.configuration.cache.IndexStorage;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RemoteQueryTest {
    static String createUri(String userName, String password) {
        return String.format(
                "hotrod://%s:%s@172.18.0.2:11222,172.18.0.3:11222,172.18.0.4:11222"
                        + "?context-initializers=org.littlewings.infinispan.remote.query.EntitiesInitializerImpl",
                userName,
                password
        );
    }

    @BeforeAll
    static void setUpAll() {
        String uri = createUri("ispn-admin", "password");

        try (RemoteCacheManager manager = new RemoteCacheManager(uri)) {
            // create permission
            manager.getConfiguration().getContextInitializers().forEach(serializationContextInitializer -> {
                RemoteCache<String, String> protoCache = manager.getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME);
                protoCache.put("entities.proto", serializationContextInitializer.getProtoFile());
            });

            RemoteCacheManagerAdmin admin = manager.administration();

            org.infinispan.configuration.cache.Configuration indexLessDistCacheConfiguration =
                    new org.infinispan.configuration.cache.ConfigurationBuilder()
                            .clustering()
                            .cacheMode(org.infinispan.configuration.cache.CacheMode.DIST_SYNC)
                            .encoding().key().mediaType("application/x-protostream")
                            .encoding().value().mediaType("application/x-protostream")
                            .build();

            admin.getOrCreateCache("bookCache", indexLessDistCacheConfiguration);

            org.infinispan.configuration.cache.Configuration indexedDistCacheConfiguration =
                    new ConfigurationBuilder()
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
                            //.path("index/indexedBookCache")
                            .startupMode(IndexStartupMode.REINDEX)
                            .reader().refreshInterval(0L)
                            .writer().commitInterval(1000)
                            .build();

            admin.getOrCreateCache("indexedBookCache", indexedDistCacheConfiguration);
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
    public void indexLessQuery() {
        this.<String, Book>withCache("bookCache", cache -> {
            List<Book> books = List.of(
                    Book.create("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 5242),
                    Book.create("978-4048917353", "Redis入門 インメモリKVSによる高速データ管理", 3400),
                    Book.create("978-4798045733", "RDB技術者のためのNoSQLガイド", 3400),
                    Book.create("978-1785285332", "Getting Started with Hazelcast - Second Edition", 4129),
                    Book.create("978-1789347531", "Apache Ignite Quick Start Guide: Distributed data caching and processing made easy", 3476)
            );

            books.forEach(book -> cache.put(book.getIsbn(), book));

            assertThat(cache.size()).isEqualTo(5);

            QueryFactory queryFactory = Search.getQueryFactory(cache);
            Query<Book> query =
                    queryFactory.create("from entity.Book where price > :price order by price desc");
            query.setParameter("price", 4000);

            List<Book> results = query.execute().list();

            assertThat(results).hasSize(2);
            assertThat(results.get(0).getTitle()).isEqualTo("Infinispan Data Grid Platform Definitive Guide");
            assertThat(results.get(1).getTitle()).isEqualTo("Getting Started with Hazelcast - Second Edition");
        });
    }

    @Test
    public void indexedQuery() {
        this.<String, IndexedBook>withCache("indexedBookCache", cache -> {
            List<IndexedBook> books = List.of(
                    IndexedBook.create("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 5242),
                    IndexedBook.create("978-4048917353", "Redis入門 インメモリKVSによる高速データ管理", 3400),
                    IndexedBook.create("978-4798045733", "RDB技術者のためのNoSQLガイド", 3400),
                    IndexedBook.create("978-1785285332", "Getting Started with Hazelcast - Second Edition", 4129),
                    IndexedBook.create("978-1789347531", "Apache Ignite Quick Start Guide: Distributed data caching and processing made easy", 3476)
            );

            books.forEach(book -> cache.put(book.getIsbn(), book));

            assertThat(cache.size()).isEqualTo(5);

            QueryFactory queryFactory = Search.getQueryFactory(cache);

            Query<IndexedBook> query1 =
                    queryFactory.create("from entity.IndexedBook where title: 'guide' and price: [4000 to *] order by price");

            List<IndexedBook> results1 = query1.execute().list();

            assertThat(results1).hasSize(1);
            assertThat(results1.get(0).getTitle()).isEqualTo("Infinispan Data Grid Platform Definitive Guide");

            Query<IndexedBook> query2 =
                    queryFactory.create("from entity.IndexedBook where isbn = '978-4048917353'");

            List<IndexedBook> results2 = query2.execute().list();

            assertThat(results2).hasSize(1);
            assertThat(results2.get(0).getTitle()).isEqualTo("Redis入門 インメモリKVSによる高速データ管理");

            Query<IndexedBook> query3 =
                    queryFactory.create("from entity.IndexedBook where (title: 'data' and title: 'guide') or price: [* to 3400] order by price desc, isbn asc");

            List<IndexedBook> results3 = query3.execute().list();

            assertThat(results3).hasSize(4);
            assertThat(results3.get(0).getTitle()).isEqualTo("Infinispan Data Grid Platform Definitive Guide");
            assertThat(results3.get(1).getTitle()).isEqualTo("Apache Ignite Quick Start Guide: Distributed data caching and processing made easy");
            assertThat(results3.get(2).getTitle()).isEqualTo("Redis入門 インメモリKVSによる高速データ管理");
            assertThat(results3.get(3).getTitle()).isEqualTo("RDB技術者のためのNoSQLガイド");
        });
    }
}
