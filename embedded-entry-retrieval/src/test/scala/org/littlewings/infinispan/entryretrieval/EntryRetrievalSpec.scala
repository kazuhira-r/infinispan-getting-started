package org.littlewings.infinispan.entryretrieval

import scala.collection.JavaConverters._

import java.util.concurrent.{ Callable, CountDownLatch, Executors }

import org.infinispan.Cache
import org.infinispan.filter.{ AcceptAllKeyValueFilter, CollectionKeyFilter, KeyFilterAsKeyValueFilter, KeyValueFilter }
import org.infinispan.manager.DefaultCacheManager

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class EntryRetrievalSpec extends FunSpec {
  describe("Distributed Entry Iterator Spec") {
    it("iteration and sum") {
      withCache[String, Integer](3, "iterationCache") { cache =>
        (1 to 20).foreach(i => cache.put(s"key$i", i))

        val iterable =
          cache.getAdvancedCache.filterEntries(AcceptAllKeyValueFilter.getInstance)
        try {
          val sum =
            iterable.asScala.foldLeft(0) { (acc, cur) => acc + cur.getValue }
          sum should be (210)
        } finally {
          iterable.close()
        }
      }
    }

    it("even key value filter") {
      withCache[String, Integer](3, "iterationCache") { cache =>
        (1 to 20).foreach(i => cache.put(s"key$i", i))

        val iterable =
          cache.getAdvancedCache.filterEntries(new EvenKeyValueFilter)
        try {
          val sum =
            iterable.asScala.foldLeft(0) { (acc, cur) => acc + cur.getValue }
          sum should be (110)
        } finally {
          iterable.close()
        }
      }
    }

    it("collection key filter") {
      withCache[String, Integer](3, "iterationCache") { cache =>
        (1 to 20).foreach(i => cache.put(s"key$i", i))

        val filter: KeyValueFilter[String, Integer] =
          new KeyFilterAsKeyValueFilter(new CollectionKeyFilter(List("key3", "key5").asJava, true))
        val iterable =
          cache.getAdvancedCache.filterEntries(filter)

        try {
          val values = iterable.asScala.map(_.getValue)
          values should contain allOf (3, 5)
        } finally {
          iterable.close()
        }
      }
    }

    it("even doubling filter and converter") {
      withCache[String, Integer](3, "iterationCache") { cache =>
        (1 to 20).foreach(i => cache.put(s"key$i", i))

        val iterable =
          cache.getAdvancedCache.filterEntries(new EvenDoublingKeyValueFilterConverter)
        try {
          val values = iterable.asScala.foldLeft(0) { (acc, cur) => acc + cur.getValue }
          values should be (220)
        } finally {
          iterable.close()
        }
      }
    }
  }

  def withCache[K, V](numInstances: Int, cacheName: String)(fun: Cache[K, V] => Unit): Unit = {
    val managers = (1 to numInstances) map (_ => new DefaultCacheManager("infinispan.xml"))
    managers.foreach(_.getCache[K, V](cacheName))

    try {
      val cache = managers.head.getCache[K, V](cacheName)
      fun(cache)
    } finally {
      managers.foreach(_.stop())
    }
  }
}
