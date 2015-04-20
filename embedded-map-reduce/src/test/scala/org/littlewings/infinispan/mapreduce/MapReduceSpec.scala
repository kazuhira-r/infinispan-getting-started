package org.littlewings.infinispan.mapreduce

import java.util.concurrent.TimeUnit

import org.infinispan.Cache
import org.infinispan.distexec.mapreduce.MapReduceTask
import org.infinispan.manager.DefaultCacheManager

import org.scalatest.Entry
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class MapReduceSpec extends FunSpec {
  describe("Map Reduce Spec") {
    it("Infinispan Examples") {
      withCache[String, String](3, "infinispan.xml", "wordCountCache") { cache =>
        registerDataToCache(cache)

        cache should have size (16)

        val task = new MapReduceTask[String, String, String, Int](cache)
        val result =
          task
            .mappedWith(new WordCountMaper)
            .reducedWith(new WordCountReducer)
            .execute

        result should have size (32)
        result should contain (Entry("is", 6))
        result should contain (Entry("Infinispan", 3))
        result should contain (Entry("RedHat", 2))
      }
    }

    it("Infinispan Example, using Collator") {
      withCache[String, String](3, "infinispan.xml", "wordCountCache") { cache =>
        registerDataToCache(cache)

        val task = new MapReduceTask[String, String, String, Int](cache)
        val result =
          task
            .mappedWith(new WordCountMaper)
            .reducedWith(new WordCountReducer)
            .execute(new TopWordCollator)

        result should be ("is")
      }
    }

    it("Result Into Cache") {
      withCache[String, String](3, "infinispan.xml", "wordCountCache") { cache =>
        registerDataToCache(cache)

        val task = new MapReduceTask[String, String, String, Int](cache)
        task
          .mappedWith(new WordCountMaper)
          .reducedWith(new WordCountReducer)
          .execute("mapReduceResultCache")

        val resultCache =
          cache.getCacheManager.getCache[String, Int]("mapReduceResultCache")

        resultCache.getCacheConfiguration.clustering.hash.numOwners should be (1)

        resultCache should have size (32)
        resultCache.get("is") should be (6)
        resultCache.get("Infinispan") should be (3)
        resultCache.get("RedHat") should be (2)
      }
    }

    it("Distributed Reduce Phase") { 
      withCache[String, String](3, "infinispan.xml", "wordCountCache") { cache =>
        registerDataToCache(cache)

        val task = new MapReduceTask[String, String, String, Int](cache, true)
        task
          .mappedWith(new WordCountMaper)
          .reducedWith(new WordCountReducer)
          .execute("mapReduceResultCache")

        val resultCache =
          cache.getCacheManager.getCache[String, Int]("mapReduceResultCache")

        resultCache should have size (32)
        resultCache.get("is") should be (6)
        resultCache.get("Infinispan") should be (3)
        resultCache.get("RedHat") should be (2)
      }
    }

    it("default time out") {
      withCache[String, String](3, "infinispan.xml", "wordCountCache") { cache =>
        val task = new MapReduceTask[String, String, String, Int](cache)

        task.timeout(TimeUnit.SECONDS) should be (0)
      }
    }

    it("configuration time out") {
      withCache[String, String](3, "infinispan.xml", "wordCountCache") { cache =>
        val task = new MapReduceTask[String, String, String, Int](cache)

        task.timeout(15, TimeUnit.SECONDS)
      }
    }

    it("Use Shared Intermediate Cache") {
      withCache[String, String](3, "infinispan.xml", "wordCountCache") { cache =>
        registerDataToCache(cache)

        val task = new MapReduceTask[String, String, String, Int](cache, true, true)
        task
          .mappedWith(new WordCountMaper)
          .reducedWith(new WordCountReducer)
          .execute("mapReduceResultCache")

        val resultCache =
          cache.getCacheManager.getCache[String, Int]("mapReduceResultCache")

        resultCache should have size (32)
        resultCache.get("is") should be (6)
        resultCache.get("Infinispan") should be (3)
        resultCache.get("RedHat") should be (2)
      }
    }

    it("Use Task Per Intermediate Cache") {
      withCache[String, String](3, "infinispan.xml", "wordCountCache") { cache =>
        registerDataToCache(cache)

        val task = new MapReduceTask[String, String, String, Int](cache, true, false)
        task
          .mappedWith(new WordCountMaper)
          .reducedWith(new WordCountReducer)
          .execute("mapReduceResultCache")

        val resultCache =
          cache.getCacheManager.getCache[String, Int]("mapReduceResultCache")

        resultCache should have size (32)
        resultCache.get("is") should be (6)
        resultCache.get("Infinispan") should be (3)
        resultCache.get("RedHat") should be (2)
      }
    }

    it("Use Shared Intermediate Cache, Spec Cache") {
      withCache[String, String](3, "infinispan.xml", "wordCountCache") { cache =>
        registerDataToCache(cache)

        val task = new MapReduceTask[String, String, String, Int](cache, true, true)
        task.usingSharedIntermediateCache("mapReduceIntermediateCache")
        task
          .mappedWith(new WordCountMaper)
          .reducedWith(new WordCountReducer)
          .execute("mapReduceResultCache")

        val resultCache =
          cache.getCacheManager.getCache[String, Int]("mapReduceResultCache")

        resultCache should have size (32)
        resultCache.get("is") should be (6)
        resultCache.get("Infinispan") should be (3)
        resultCache.get("RedHat") should be (2)
      }
    }

    it("Use Shared Intermediate Cache, Spec Cache and Cache Configuration") {
      withCache[String, String](3, "infinispan.xml", "wordCountCache") { cache =>
        registerDataToCache(cache)

        val task = new MapReduceTask[String, String, String, Int](cache, true, true)
        // Shared Intermediate Cacheとして定義したCacheは、残り続ける
        task.usingSharedIntermediateCache("mapReduceTmpCache", "mapReduceIntermediateCache")
        task
          .mappedWith(new WordCountMaper)
          .reducedWith(new WordCountReducer)
          .execute("mapReduceResultCache")

        val resultCache =
          cache.getCacheManager.getCache[String, Int]("mapReduceResultCache")

        resultCache should have size (32)
        resultCache.get("is") should be (6)
        resultCache.get("Infinispan") should be (3)
        resultCache.get("RedHat") should be (2)
      }
    }

    it("Use Task Per Intermediate Cache, Spec Cache Configuration") {
      withCache[String, String](3, "infinispan.xml", "wordCountCache") { cache =>
        registerDataToCache(cache)

        val task = new MapReduceTask[String, String, String, Int](cache, true, false)
        // 内部的に、UUID#randomUUIDを使った名前でCacheが作成され、タスク終了時に削除される
        task.usingIntermediateCache("mapReduceIntermediateCache")
        task
          .mappedWith(new WordCountMaper)
          .reducedWith(new WordCountReducer)
          .execute("mapReduceResultCache")

        val resultCache =
          cache.getCacheManager.getCache[String, Int]("mapReduceResultCache")

        resultCache should have size (32)
        resultCache.get("is") should be (6)
        resultCache.get("Infinispan") should be (3)
        resultCache.get("RedHat") should be (2)
      }
    }
  }

  private def registerDataToCache(cache: Cache[String, String]): Unit = {
    cache.put("1", "Hello world here I am")
    cache.put("2", "Infinispan rules the world")
    cache.put("3", "JUDCon is in Boston")
    cache.put("4", "JBoss World is in Boston as well")
    cache.put("12","JBoss Application Server")
    cache.put("13", "Hello world")
    cache.put("14", "Infinispan community");
    cache.put("16", "Hello world")

    cache.put("111", "Infinispan open source")
    cache.put("112", "Boston is close to Toronto")
    cache.put("113", "Toronto is a capital of Ontario")
    cache.put("114", "JUDCon is cool")
    cache.put("211", "JBoss World is awesome");
    cache.put("212", "JBoss rules")
    cache.put("213", "JBoss division of RedHat ")
    cache.put("214", "RedHat community")
  }

  protected def withCache[K, V](numInstances: Int, configurationFile: String, cacheName: String)(fun: Cache[K, V] => Unit): Unit = {
    val managers = (1 to numInstances).map(_ => new DefaultCacheManager(configurationFile))

    try {
      managers.foreach(_.getCache[K, V](cacheName))

      val manager = managers.head
      val cache = manager.getCache[K, V](cacheName)

      fun(cache)

      cache.stop()
    } finally {
      managers.foreach(_.stop())
    }
  }
}
