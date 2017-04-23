package org.littlewings.infinispan.query.affinityindex

import org.infinispan.Cache
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.query.Search
import org.infinispan.query.affinity.ShardAllocatorManager
import org.scalatest.{FunSuite, Matchers}

class AffinityIndexQuerySpec extends FunSuite with Matchers {
  val books: Array[Book] = Array(
    Book("978-4798142470", "Spring徹底入門 Spring FrameworkによるJavaアプリケーション開発", 4320),
    Book("978-4774182179", "［改訂新版］Spring入門 ――Javaフレームワーク・より良い設計とアーキテクチャ", 4104),
    Book("978-4774161631", "[改訂新版] Apache Solr入門 ~オープンソース全文検索エンジン", 3888),
    Book("978-4048662024", "高速スケーラブル検索エンジン ElasticSearch Server", 6915),
    Book("978-4774183169", "パーフェクト Java EE", 3456),
    Book("978-4798140926", "Java EE 7徹底入門 標準Javaフレームワークによる高信頼性Webシステムの構築", 4104)
  )

  test("indexing local only") {
    withCache[String, Book]("bookCache", 3) { cache =>
      books.foreach(b => cache.put(b.isbn, b))

      val qf = Search.getQueryFactory(cache)
      val query =
        qf.create(s"from ${classOf[Book].getName} b where b.title: (+'検索' or +'構築') order by b.price desc")

      val results = query.list[Book]
      results should have size (3)
      results.get(0).title should be("高速スケーラブル検索エンジン ElasticSearch Server")
      results.get(1).title should be("Java EE 7徹底入門 標準Javaフレームワークによる高信頼性Webシステムの構築")
      results.get(2).title should be("[改訂新版] Apache Solr入門 ~オープンソース全文検索エンジン")
    }
  }

  test("using affinity-index-manager") {
    withCache[String, Book]("affinityIndexBookCache", 3) { cache =>
      books.foreach(b => cache.put(b.isbn, b))

      val qf = Search.getQueryFactory(cache)
      val query =
        qf.create(s"from ${classOf[Book].getName} b where b.title: (+'検索' or +'構築') order by b.price desc")

      val results = query.list[Book]
      results should have size (3)
      results.get(0).title should be("高速スケーラブル検索エンジン ElasticSearch Server")
      results.get(1).title should be("Java EE 7徹底入門 標準Javaフレームワークによる高信頼性Webシステムの構築")
      results.get(2).title should be("[改訂新版] Apache Solr入門 ~オープンソース全文検索エンジン")
    }
  }

  test("using affinity-index-manager and shard spec") {
    withCache[String, Book]("shardSpecAffinityIndexBookCache", 3) { cache =>
      books.foreach(b => cache.put(b.isbn, b))

      val qf = Search.getQueryFactory(cache)
      val query =
        qf.create(s"from ${classOf[Book].getName} b where b.title: (+'検索' or +'構築') order by b.price desc")

      val results = query.list[Book]
      results should have size (3)
      results.get(0).title should be("高速スケーラブル検索エンジン ElasticSearch Server")
      results.get(1).title should be("Java EE 7徹底入門 標準Javaフレームワークによる高信頼性Webシステムの構築")
      results.get(2).title should be("[改訂新版] Apache Solr入門 ~オープンソース全文検索エンジン")
    }
  }

  test("indexing local only, reindex") {
    withCache[String, Book]("bookCache", 3) { cache =>
      books.foreach(b => cache.put(b.isbn, b))

      val searchManager = Search.getSearchManager(cache)
      searchManager.getMassIndexer.start()
    }
  }

  test("using affinity-index-manager, reindex") {
    withCache[String, Book]("affinityIndexBookCache", 3) { cache =>
      books.foreach(b => cache.put(b.isbn, b))

      val searchManager = Search.getSearchManager(cache)
      searchManager.getMassIndexer.start()
    }
  }

  test("using affinity-index-manager and shard spec, reindex") {
    withCache[String, Book]("shardSpecAffinityIndexBookCache", 3) { cache =>
      books.foreach(b => cache.put(b.isbn, b))

      val searchManager = Search.getSearchManager(cache)
      searchManager.getMassIndexer.start()
    }
  }

  test("indexing local only, number of shards") {
    withCache[String, Book]("bookCache", 3) { cache =>
      books.foreach(b => cache.put(b.isbn, b))

      val shardAllocatorManager =
        cache.getAdvancedCache.getComponentRegistry.getComponent(classOf[ShardAllocatorManager])
      shardAllocatorManager.getShards should have size (256)
      shardAllocatorManager.getShards should contain only ((0 until 256).map(_.toString): _*)
    }
  }

  test("using affinity-index-manager, number of shards") {
    withCache[String, Book]("affinityIndexBookCache", 3) { cache =>
      books.foreach(b => cache.put(b.isbn, b))

      val shardAllocatorManager =
        cache.getAdvancedCache.getComponentRegistry.getComponent(classOf[ShardAllocatorManager])
      shardAllocatorManager.getShards should have size (256)
      shardAllocatorManager.getShards should contain only ((0 until 256).map(_.toString): _*)
    }
  }

  test("using affinity-index-manager and shard spec, number of shards") {
    withCache[String, Book]("shardSpecAffinityIndexBookCache", 3) { cache =>
      books.foreach(b => cache.put(b.isbn, b))

      val shardAllocatorManager =
        cache.getAdvancedCache.getComponentRegistry.getComponent(classOf[ShardAllocatorManager])
      shardAllocatorManager.getShards should have size (9)
      shardAllocatorManager.getShards should contain only ((0 until 9).map(_.toString): _*)
    }
  }

  test("indexing local only, print shards and segments") {
    withCache[String, Book]("bookCache", 3) { cache =>
      books.foreach(b => cache.put(b.isbn, b))

      val shardAllocatorManager =
        cache.getAdvancedCache.getComponentRegistry.getComponent(classOf[ShardAllocatorManager])
      val cacheManager = cache.getCacheManager

      println("===== LuceneIndexesLocking =====")
      val luceneIndexesLockingCache = cacheManager.getCache[AnyRef, AnyRef]("LuceneIndexesLocking")
      luceneIndexesLockingCache
        .forEach { case (k, v) =>
          val cacheTopology = luceneIndexesLockingCache.getAdvancedCache.getDistributionManager.getCacheTopology
          val shard = shardAllocatorManager.getShardFromKey(k)
          println(s"${k}, segment = ${cacheTopology.getSegment(k)}, shard = ${shard}")
        }
      println("===== LuceneIndexesMetadata =====")
      val luceneIndexesMetadataCache = cacheManager.getCache[AnyRef, AnyRef]("LuceneIndexesMetadata")
      luceneIndexesMetadataCache
        .forEach { case (k, v) =>
          val cacheTopology = luceneIndexesMetadataCache.getAdvancedCache.getDistributionManager.getCacheTopology
          val shard = shardAllocatorManager.getShardFromKey(k)
          println(s"${k}, segment = ${cacheTopology.getSegment(k)}, shard = ${shard}")
        }
      println("===== LuceneIndexesData =====")
      val luceneIndexesDataCache = cacheManager.getCache[AnyRef, AnyRef]("LuceneIndexesData")
      luceneIndexesDataCache
        .forEach { case (k, v) =>
          val cacheTopology = luceneIndexesDataCache.getAdvancedCache.getDistributionManager.getCacheTopology
          val shard = shardAllocatorManager.getShardFromKey(k)
          println(s"${k}, segment = ${cacheTopology.getSegment(k)}, shard = ${shard}")
        }
    }
  }

  test("using affinity-index-manager, print shards and segments") {
    withCache[String, Book]("affinityIndexBookCache", 3) { cache =>
      books.foreach(b => cache.put(b.isbn, b))

      val shardAllocatorManager =
        cache.getAdvancedCache.getComponentRegistry.getComponent(classOf[ShardAllocatorManager])
      val cacheManager = cache.getCacheManager

      println("===== LuceneIndexesLocking =====")
      val luceneIndexesLockingCache = cacheManager.getCache[AnyRef, AnyRef]("LuceneIndexesLocking")
      luceneIndexesLockingCache
        .forEach { case (k, v) =>
          val cacheTopology = luceneIndexesLockingCache.getAdvancedCache.getDistributionManager.getCacheTopology
          val shard = shardAllocatorManager.getShardFromKey(k)
          println(s"${k}, segment = ${cacheTopology.getSegment(k)}, shard = ${shard}")
        }
      println("===== LuceneIndexesMetadata =====")
      val luceneIndexesMetadataCache = cacheManager.getCache[AnyRef, AnyRef]("LuceneIndexesMetadata")
      luceneIndexesMetadataCache
        .forEach { case (k, v) =>
          val cacheTopology = luceneIndexesMetadataCache.getAdvancedCache.getDistributionManager.getCacheTopology
          val shard = shardAllocatorManager.getShardFromKey(k)
          println(s"${k}, segment = ${cacheTopology.getSegment(k)}, shard = ${shard}")
        }
      println("===== LuceneIndexesData =====")
      val luceneIndexesDataCache = cacheManager.getCache[AnyRef, AnyRef]("LuceneIndexesData")
      luceneIndexesDataCache
        .forEach { case (k, v) =>
          val cacheTopology = luceneIndexesDataCache.getAdvancedCache.getDistributionManager.getCacheTopology
          val shard = shardAllocatorManager.getShardFromKey(k)
          println(s"${k}, segment = ${cacheTopology.getSegment(k)}, shard = ${shard}")
        }
    }
  }

  test("using affinity-index-manager and shard spec, print shards and segments") {
    withCache[String, Book]("shardSpecAffinityIndexBookCache", 3) { cache =>
      books.foreach(b => cache.put(b.isbn, b))

      val shardAllocatorManager =
        cache.getAdvancedCache.getComponentRegistry.getComponent(classOf[ShardAllocatorManager])
      val cacheManager = cache.getCacheManager

      println("===== LuceneIndexesLocking =====")
      val luceneIndexesLockingCache = cacheManager.getCache[AnyRef, AnyRef]("LuceneIndexesLocking")
      luceneIndexesLockingCache
        .forEach { case (k, v) =>
          val cacheTopology = luceneIndexesLockingCache.getAdvancedCache.getDistributionManager.getCacheTopology
          val shard = shardAllocatorManager.getShardFromKey(k)
          println(s"${k}, segment = ${cacheTopology.getSegment(k)}, shard = ${shard}")
        }
      println("===== LuceneIndexesMetadata =====")
      val luceneIndexesMetadataCache = cacheManager.getCache[AnyRef, AnyRef]("LuceneIndexesMetadata")
      luceneIndexesMetadataCache
        .forEach { case (k, v) =>
          val cacheTopology = luceneIndexesMetadataCache.getAdvancedCache.getDistributionManager.getCacheTopology
          val shard = shardAllocatorManager.getShardFromKey(k)
          println(s"${k}, segment = ${cacheTopology.getSegment(k)}, shard = ${shard}")
        }
      println("===== LuceneIndexesData =====")
      val luceneIndexesDataCache = cacheManager.getCache[AnyRef, AnyRef]("LuceneIndexesData")
      luceneIndexesDataCache
        .forEach { case (k, v) =>
          val cacheTopology = luceneIndexesDataCache.getAdvancedCache.getDistributionManager.getCacheTopology
          val shard = shardAllocatorManager.getShardFromKey(k)
          println(s"${k}, segment = ${cacheTopology.getSegment(k)}, shard = ${shard}")
        }
    }
  }

  protected def withCache[K, V](cacheName: String, numInstances: Int = 1)(fun: Cache[K, V] => Unit): Unit = {
    val managers = (1 to numInstances).map(_ => new DefaultCacheManager("infinispan.xml"))
    managers.foreach(_.getCache[K, V](cacheName))

    try {
      val cache = managers(0).getCache[K, V](cacheName)
      fun(cache)
      cache.stop()
    } finally {
      managers.foreach(_.stop())
    }
  }
}
