package org.littlewings.infinispan.query

import org.apache.lucene.search.{Sort, SortField}
import org.infinispan.Cache
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.query.{CacheQuery, Search}
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class EmbeddedClusteredIndexUpdateSpec extends FunSpec {
  describe("EmbeddedClusteredIndexUpdateSpec") {
    it("update indexes") {
      withCache[String, Contents]("queryCache") { firstCache =>
        val entries1 = List(
          Contents("1", "はじめてのSpring Boot"),
          Contents("2", "高速スケーラブル検索エンジン ElasticSearch Server"),
          Contents("3", "わかりやすいJava EE ウェブシステム入門")
        )

        entries1.foreach(e => firstCache.put(e.id, e))

        val firstCacheQuery1 = createQuery(firstCache, classOf[Contents], "Spring")

        val firstResult1 = firstCacheQuery1.list()
        firstCacheQuery1.getResultSize should be(1)
        firstResult1.get(0) should be(Contents("1", "はじめてのSpring Boot"))

        withCache[String, Contents]("queryCache") { secondCache =>
          val secondCacheQuery1 = createQuery(secondCache, classOf[Contents], "ElasticSearch")

          val secondResult1 = secondCacheQuery1.list()
          secondCacheQuery1.getResultSize should be(1)
          secondResult1.get(0) should be(Contents("2", "高速スケーラブル検索エンジン ElasticSearch Server"))

          val entries2 = List(
            Contents("4", "[改訂新版] Apache Solr入門 ～オープンソース全文検索エンジン"),
            Contents("5", "Beginning Java EE 6 GlassFish 3で始めるエンタープライズJava (Programmer’s SELECTION)"),
            Contents("6", "Spring3入門 ――Javaフレームワーク・より良い設計とアーキテクチャ")
          )

          entries2.foreach(e => secondCache.put(e.id, e))

          val secondCacheQuery2 = createQuery(secondCache, classOf[Contents], "Apache Solr")

          val secondResult2 = secondCacheQuery2.list()
          secondCacheQuery2.getResultSize should be(1)
          secondResult2.get(0) should be(Contents("4", "[改訂新版] Apache Solr入門 ～オープンソース全文検索エンジン"))
        }

        val firstCacheQuery2 = createQuery(firstCache, classOf[Contents], "Spring")

        val resultMaster2 = firstCacheQuery2.list()
        firstCacheQuery2.getResultSize should be(2)
        resultMaster2.get(0) should be(Contents("1", "はじめてのSpring Boot"))
        resultMaster2.get(1) should be(Contents("6", "Spring3入門 ――Javaフレームワーク・より良い設計とアーキテクチャ"))
      }
    }

    it("rebuild index") {
      withCache[String, Contents]("queryCache") { firstCache =>
        val entries1 = List(
          Contents("1", "はじめてのSpring Boot"),
          Contents("2", "高速スケーラブル検索エンジン ElasticSearch Server"),
          Contents("3", "わかりやすいJava EE ウェブシステム入門")
        )

        entries1.foreach(e => firstCache.put(e.id, e))

        rebuildIndex(firstCache)

        val firstCacheQuery1 = createQuery(firstCache, classOf[Contents], "Spring")

        val firstResult1 = firstCacheQuery1.list()
        firstCacheQuery1.getResultSize should be(1)
        firstResult1.get(0) should be(Contents("1", "はじめてのSpring Boot"))

        withCache[String, Contents]("queryCache") { secondCache =>
          val secondCacheQuery1 = createQuery(secondCache, classOf[Contents], "ElasticSearch")

          val secondResult1 = secondCacheQuery1.list()
          secondCacheQuery1.getResultSize should be(1)
          secondResult1.get(0) should be(Contents("2", "高速スケーラブル検索エンジン ElasticSearch Server"))

          val entries2 = List(
            Contents("4", "[改訂新版] Apache Solr入門 ～オープンソース全文検索エンジン"),
            Contents("5", "Beginning Java EE 6 GlassFish 3で始めるエンタープライズJava (Programmer’s SELECTION)"),
            Contents("6", "Spring3入門 ――Javaフレームワーク・より良い設計とアーキテクチャ")
          )

          entries2.foreach(e => secondCache.put(e.id, e))

          rebuildIndex(secondCache)

          val secondCacheQuery2 = createQuery(secondCache, classOf[Contents], "Apache Solr")

          val secondResult2 = secondCacheQuery2.list()
          secondCacheQuery2.getResultSize should be(1)
          secondResult2.get(0) should be(Contents("4", "[改訂新版] Apache Solr入門 ～オープンソース全文検索エンジン"))
        }

        val firstCacheQuery2 = createQuery(firstCache, classOf[Contents], "Spring")

        val resultMaster2 = firstCacheQuery2.list()
        firstCacheQuery2.getResultSize should be(2)
        resultMaster2.get(0) should be(Contents("1", "はじめてのSpring Boot"))
        resultMaster2.get(1) should be(Contents("6", "Spring3入門 ――Javaフレームワーク・より良い設計とアーキテクチャ"))
      }
    }
  }

  protected def createQuery(cache: Cache[_, _], entityClass: Class[_], queryWord: String): CacheQuery = {
    val searchManager = Search.getSearchManager(cache)
    val query =
      searchManager
        .buildQueryBuilderForClass(entityClass)
        .get
        .keyword()
        .onField("value")
        .matching(queryWord)
        .createQuery

    searchManager
      .getQuery(query, entityClass)
      .sort(new Sort(new SortField("id", SortField.Type.INT)))
  }

  protected def rebuildIndex(cache: Cache[_, _]): Unit =
    Search.getSearchManager(cache).getMassIndexer.start()

  protected def withCache[K, V](cacheName: String, numInstances: Int = 1)(f: Cache[K, V] => Unit): Unit = {
    val managers = (1 to numInstances).map(_ => new DefaultCacheManager("infinispan.xml"))

    try {
      val caches = managers.map(_.getCache[K, V](cacheName))

      f(caches.head)

      caches.foreach(_.stop)
    } finally {
      managers.foreach(_.stop)
    }
  }
}
