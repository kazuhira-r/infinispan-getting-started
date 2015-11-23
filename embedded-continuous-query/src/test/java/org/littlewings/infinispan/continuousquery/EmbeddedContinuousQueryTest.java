package org.littlewings.infinispan.continuousquery;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.assertj.core.data.MapEntry;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.query.Search;
import org.infinispan.query.continuous.ContinuousQuery;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EmbeddedContinuousQueryTest {
    @Test
    public void testContinuousQuerySimply() {
        withCache("distCache", 3, (Cache<String, Book> cache) -> {
            QueryFactory queryFactory = Search.getQueryFactory(cache);
            Query query =
                    queryFactory
                            .from(Book.class)
                            .having("price")
                            .gte(3000)
                            .toBuilder()
                            .build();

            MyContinuousQueryListener<String, Book> myCqListener = new MyContinuousQueryListener<>();

            ContinuousQuery<String, Book> cq = new ContinuousQuery<>(cache);
            cq.addContinuousQueryListener(query, myCqListener);

            /////
            Book sprintBoot =
                    Book
                            .create("978-4777518654",
                                    "はじめてのSpring Boot 「Spring Framework」で簡単Javaアプリ開発",
                                    2700,
                                    "Java", "Spring");

            cache.put(sprintBoot.getIsbn(), sprintBoot);

            assertThat(myCqListener.getJoined())
                    .isEmpty();
            assertThat(myCqListener.getJoinCalled())
                    .isEmpty();
            assertThat(myCqListener.getLeaveCalled())
                    .isEmpty();

            /////
            Book elasticsearch =
                    Book
                            .create("978-4048662024",
                                    "高速スケーラブル検索エンジン ElasticSearch Server",
                                    3024,
                                    "Elasticsearch", "全文検索", "Java", "Lucene");

            cache.put(elasticsearch.getIsbn(), elasticsearch);

            assertThat(myCqListener.getJoined())
                    .hasSize(1)
                    .containsOnly(MapEntry.entry(elasticsearch.getIsbn(), elasticsearch));
            assertThat(myCqListener.getJoinCalled())
                    .hasSize(1)
                    .containsOnly(MapEntry.entry(elasticsearch.getIsbn(), 1));
            assertThat(myCqListener.getLeaveCalled())
                    .isEmpty();

            /////
            Book solr =
                    Book
                            .create("978-4774161631",
                                    "[改訂新版] Apache Solr入門 ～オープンソース全文検索エンジン",
                                    3888,
                                    "Solr", "全文検索", "Java", "Lucene");

            cache.put(solr.getIsbn(), solr);

            assertThat(myCqListener.getJoined())
                    .hasSize(2)
                    .containsOnly(MapEntry.entry(elasticsearch.getIsbn(), elasticsearch),
                            MapEntry.entry(solr.getIsbn(), solr));
            assertThat(myCqListener.getJoinCalled())
                    .hasSize(2)
                    .containsOnly(MapEntry.entry(elasticsearch.getIsbn(), 1),
                            MapEntry.entry(solr.getIsbn(), 1));
            assertThat(myCqListener.getLeaveCalled())
                    .isEmpty();

            /////
            Book jbossEap =
                    Book
                            .create("978-4774157948",
                                    "JBoss Enterprise Application Platform6 構築・運用パーフェクトガイド",
                                    4104,
                                    "Java");

            cache.put(jbossEap.getIsbn(), jbossEap);

            assertThat(myCqListener.getJoined())
                    .hasSize(3)
                    .containsOnly(MapEntry.entry(elasticsearch.getIsbn(), elasticsearch),
                            MapEntry.entry(solr.getIsbn(), solr),
                            MapEntry.entry(jbossEap.getIsbn(), jbossEap));
            assertThat(myCqListener.getJoinCalled())
                    .hasSize(3)
                    .containsOnly(MapEntry.entry(elasticsearch.getIsbn(), 1),
                            MapEntry.entry(solr.getIsbn(), 1),
                            MapEntry.entry(jbossEap.getIsbn(), 1));
            assertThat(myCqListener.getLeaveCalled())
                    .isEmpty();

            /////
            Book javaBook =
                    Book
                            .create("978-4774169316",
                                    "Javaエンジニア養成読本 [現場で役立つ最新知識、満載!]",
                                    2138,
                                    "Java");

            cache.put(javaBook.getIsbn(), javaBook);

            assertThat(myCqListener.getJoined())
                    .hasSize(3)
                    .containsOnly(MapEntry.entry(elasticsearch.getIsbn(), elasticsearch),
                            MapEntry.entry(solr.getIsbn(), solr),
                            MapEntry.entry(jbossEap.getIsbn(), jbossEap));
            assertThat(myCqListener.getJoinCalled())
                    .hasSize(3)
                    .containsOnly(MapEntry.entry(elasticsearch.getIsbn(), 1),
                            MapEntry.entry(solr.getIsbn(), 1),
                            MapEntry.entry(jbossEap.getIsbn(), 1));
            assertThat(myCqListener.getLeaveCalled())
                    .isEmpty();

            /////
            cache.remove(elasticsearch.getIsbn());

            assertThat(cache.get(elasticsearch.getIsbn()))
                    .isNull();
            assertThat(myCqListener.getJoined())
                    .hasSize(3)
                    .containsOnly(MapEntry.entry(elasticsearch.getIsbn(), elasticsearch),
                            MapEntry.entry(solr.getIsbn(), solr),
                            MapEntry.entry(jbossEap.getIsbn(), jbossEap));
            assertThat(myCqListener.getJoinCalled())
                    .hasSize(3)
                    .containsOnly(MapEntry.entry(elasticsearch.getIsbn(), 1),
                            MapEntry.entry(solr.getIsbn(), 1),
                            MapEntry.entry(jbossEap.getIsbn(), 1));
            assertThat(myCqListener.getLeaveCalled())
                    .hasSize(1)
                    .containsOnly(MapEntry.entry(elasticsearch.getIsbn(), 1));

            /////
            Book newSolr =
                    Book
                            .create("978-4774161631",
                                    "[改訂新版] Apache Solr入門 ～オープンソース全文検索エンジン",
                                    4000,
                                    "Solr", "全文検索", "Java", "Lucene");
            cache.put(newSolr.getIsbn(), newSolr);

            assertThat(myCqListener.getJoined())
                    .hasSize(3)
                    .containsOnly(MapEntry.entry(elasticsearch.getIsbn(), elasticsearch),
                            MapEntry.entry(solr.getIsbn(), solr),
                            MapEntry.entry(jbossEap.getIsbn(), jbossEap));
            assertThat(myCqListener.getJoinCalled())
                    .hasSize(3)
                    .containsOnly(MapEntry.entry(elasticsearch.getIsbn(), 1),
                            MapEntry.entry(solr.getIsbn(), 1),
                            MapEntry.entry(jbossEap.getIsbn(), 1));
            assertThat(myCqListener.getLeaveCalled())
                    .hasSize(1)
                    .containsOnly(MapEntry.entry(elasticsearch.getIsbn(), 1));

            /////
            cache.remove(sprintBoot.getIsbn());

            assertThat(cache.get(sprintBoot.getIsbn()))
                    .isNull();
            assertThat(myCqListener.getJoined())
                    .hasSize(3)
                    .containsOnly(MapEntry.entry(elasticsearch.getIsbn(), elasticsearch),
                            MapEntry.entry(solr.getIsbn(), solr),
                            MapEntry.entry(jbossEap.getIsbn(), jbossEap));
            assertThat(myCqListener.getJoinCalled())
                    .hasSize(3)
                    .containsOnly(MapEntry.entry(elasticsearch.getIsbn(), 1),
                            MapEntry.entry(solr.getIsbn(), 1),
                            MapEntry.entry(jbossEap.getIsbn(), 1));
            assertThat(myCqListener.getLeaveCalled())
                    .hasSize(1)
                    .containsOnly(MapEntry.entry(elasticsearch.getIsbn(), 1));
        });
    }

    @Test
    public void testContinuousQueryNested() {
        withCache("distCache", 3, (Cache<String, Book> cache) -> {
            QueryFactory queryFactory = Search.getQueryFactory(cache);
            Query query =
                    queryFactory
                            .from(Book.class)
                            .having("tags.name")
                            .eq("全文検索")
                            .toBuilder()
                            .build();

            MyContinuousQueryListener<String, Book> myCqListener = new MyContinuousQueryListener<>();

            ContinuousQuery<String, Book> cq = new ContinuousQuery<>(cache);
            cq.addContinuousQueryListener(query, myCqListener);

            /////
            Book sprintBoot =
                    Book
                            .create("978-4777518654",
                                    "はじめてのSpring Boot 「Spring Framework」で簡単Javaアプリ開発",
                                    2700,
                                    "Java", "Spring");

            cache.put(sprintBoot.getIsbn(), sprintBoot);

            assertThat(myCqListener.getJoined())
                    .isEmpty();
            assertThat(myCqListener.getJoinCalled())
                    .isEmpty();
            assertThat(myCqListener.getLeaveCalled())
                    .isEmpty();

            /////
            Book elasticsearch =
                    Book
                            .create("978-4048662024",
                                    "高速スケーラブル検索エンジン ElasticSearch Server",
                                    3024,
                                    "Elasticsearch", "全文検索", "Java", "Lucene");

            cache.put(elasticsearch.getIsbn(), elasticsearch);

            assertThat(myCqListener.getJoined())
                    .hasSize(1)
                    .containsOnly(MapEntry.entry(elasticsearch.getIsbn(), elasticsearch));
            assertThat(myCqListener.getJoinCalled())
                    .hasSize(1)
                    .containsOnly(MapEntry.entry(elasticsearch.getIsbn(), 1));
            assertThat(myCqListener.getLeaveCalled())
                    .isEmpty();

            /////
            Book solr =
                    Book
                            .create("978-4774161631",
                                    "[改訂新版] Apache Solr入門 ～オープンソース全文検索エンジン",
                                    3888,
                                    "Solr", "全文検索", "Java", "Lucene");

            cache.put(solr.getIsbn(), solr);

            assertThat(myCqListener.getJoined())
                    .hasSize(2)
                    .containsOnly(MapEntry.entry(elasticsearch.getIsbn(), elasticsearch),
                            MapEntry.entry(solr.getIsbn(), solr));
            assertThat(myCqListener.getJoinCalled())
                    .hasSize(2)
                    .containsOnly(MapEntry.entry(elasticsearch.getIsbn(), 1),
                            MapEntry.entry(solr.getIsbn(), 1));
            assertThat(myCqListener.getLeaveCalled())
                    .isEmpty();

            /////
            cache.remove(elasticsearch.getIsbn());

            assertThat(cache.get(elasticsearch.getIsbn()))
                    .isNull();
            assertThat(myCqListener.getJoined())
                    .hasSize(2)
                    .containsOnly(MapEntry.entry(elasticsearch.getIsbn(), elasticsearch),
                            MapEntry.entry(solr.getIsbn(), solr));
            assertThat(myCqListener.getJoinCalled())
                    .hasSize(2)
                    .containsOnly(MapEntry.entry(elasticsearch.getIsbn(), 1),
                            MapEntry.entry(solr.getIsbn(), 1));
            assertThat(myCqListener.getLeaveCalled())
                    .hasSize(1)
                    .containsOnly(MapEntry.entry(elasticsearch.getIsbn(), 1));

            /////
            cache.remove(sprintBoot.getIsbn());

            assertThat(cache.get(sprintBoot.getIsbn()))
                    .isNull();
            assertThat(myCqListener.getJoined())
                    .hasSize(2)
                    .containsOnly(MapEntry.entry(elasticsearch.getIsbn(), elasticsearch),
                            MapEntry.entry(solr.getIsbn(), solr));
            assertThat(myCqListener.getJoinCalled())
                    .hasSize(2)
                    .containsOnly(MapEntry.entry(elasticsearch.getIsbn(), 1),
                            MapEntry.entry(solr.getIsbn(), 1));
            assertThat(myCqListener.getLeaveCalled())
                    .hasSize(1)
                    .containsOnly(MapEntry.entry(elasticsearch.getIsbn(), 1));
        });
    }

    protected <K, V> void withCache(String cacheName, int numberOfInstances, Consumer<Cache<K, V>> consumer) {
        List<EmbeddedCacheManager> cacheManagers =
                IntStream
                        .rangeClosed(1, numberOfInstances)
                        .mapToObj(i -> {
                            try {
                                return new DefaultCacheManager("infinispan.xml");
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        })
                        .collect(Collectors.toList());

        List<Cache<K, V>> caches =
                cacheManagers
                        .stream()
                        .map(m -> m.<K, V>getCache(cacheName))
                        .collect(Collectors.toList());

        try {
            consumer.accept(caches.get(0));
        } finally {
            caches.forEach(Cache::stop);
            cacheManagers.forEach(EmbeddedCacheManager::stop);
        }
    }
}
