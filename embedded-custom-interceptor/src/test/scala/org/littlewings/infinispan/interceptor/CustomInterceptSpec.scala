package org.littlewings.infinispan.interceptor

import org.infinispan.{ AdvancedCache, Cache }
import org.infinispan.manager.DefaultCacheManager

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class CustomInterceptorSpec extends FunSpec {
  describe("Infinispan Custom Interceptor Spec") {
    it("PutKeyValue Trace Interceptor") {
      withCache[String, String]("infinispan.xml", "withPutKeyValueTraceCache") { cache =>
        (1 to 3).foreach(i => cache.put(s"key$i", s"value$i"))

        cache.get("key1") should be ("value1")
        cache.get("key2") should be ("value2")
        cache.get("key3") should be ("value3")
      }
    }

    it("PutKeyValue, GetKeyVakue, Trace Interceptor") {
      withCache[String, String]("infinispan.xml", "withPutGetTraceCache") { cache =>
        (1 to 3).foreach(i => cache.put(s"key$i", s"value$i"))

        cache.get("key1") should be ("value1")
        cache.get("key2") should be ("value2")
        cache.get("key3") should be ("value3")
      }
    }

    it("PutKeyValue, GetKeyVakue, Trace Interceptor, add AdvancedCache") {
      withCache[String, String]("infinispan.xml", "noInterceptorCache") { cache =>
        val advancedCache: AdvancedCache[String, String] = cache.getAdvancedCache
        advancedCache.addInterceptor(new PutKeyValueTraceInterceptor, 0)
        advancedCache.addInterceptor(new GetKeyValueTraceInterceptor, 1)

        (1 to 3).foreach(i => cache.put(s"key$i", s"value$i"))

        cache.get("key1") should be ("value1")
        cache.get("key2") should be ("value2")
        cache.get("key3") should be ("value3")
      }
    }

    it("Put, Get, Trace Interceptor") {
      withCache[String, String]("infinispan.xml", "withTraceCache") { cache =>
        (1 to 3).foreach(i => cache.put(s"key$i", s"value$i"))

        cache.get("key1") should be ("value1")
        cache.get("key2") should be ("value2")
        cache.get("key3") should be ("value3")
      }
    }

    it("Multiply, Trace Interceptor") {
      withCache[String, Int]("infinispan.xml", "withMultiplyAndTraceCache") { cache =>
        (1 to 3).foreach(i => cache.put(s"key$i", i))

        cache.get("key1") should be (10)
        cache.get("key2") should be (20)
        cache.get("key3") should be (30)
      }
    }
  }

  private def withCache[K, V](fileName: String, cacheName: String)(f: Cache[K, V] => Unit): Unit = {
    val manager = new DefaultCacheManager(fileName)

    try {
      val cache = manager.getCache[K, V](cacheName)

      f(cache)

      cache.stop()
    } finally {
      manager.stop()
    }
  }
}
