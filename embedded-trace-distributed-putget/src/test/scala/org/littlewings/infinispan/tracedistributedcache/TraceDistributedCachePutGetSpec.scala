package org.littlewings.infinispan.tracedistributedcache

import org.infinispan.Cache
import org.infinispan.manager.DefaultCacheManager
import org.scalatest.{FunSpec, Matchers}

class TraceDistributedCachePutGetSpec extends FunSpec with Matchers {
  describe("Trace DistributedCache Spec") {
    it("trace, self assigned key, get") {
      withCache[String, String]("distributedCache", 3) { cache =>
        val keyRange = 1 to 10
        keyRange.foreach { i =>
          println(s"===== PUT KEY = key$i START")
          cache.put(s"key$i", s"value$i")
          println(s"===== PUT KEY = key$i END")
        }

        val keys = keyRange.map(i => s"key$i").toVector

        val dm = cache.getAdvancedCache.getDistributionManager
        val self = cache.getAdvancedCache.getRpcManager.getAddress

        val pickAssignedSelfKey = keys.find(key => dm.getLocality(key).isLocal)

        pickAssignedSelfKey should not be(None)

        pickAssignedSelfKey.foreach { key =>
          println(s"===== ASSIGNED KEY = $key START =====")
          cache.get(key) should be(key.replace("key", "value"))
          println(s"===== ASSIGNED KEY = $key END =====")

          println(s"===== RE LOOKUP KEY = $key START =====")
          cache.get(key) should be(key.replace("key", "value"))
          println(s"===== RE LOOKUP KEY = $key END =====")
        }
      }
    }

    it("trace, not self assigned key, get") {
      withCache[String, String]("distributedCache", 3) { cache =>
        val keyRange = 1 to 10
        keyRange.foreach { i =>
          println(s"===== PUT KEY = key$i START")
          cache.put(s"key$i", s"value$i")
          println(s"===== PUT KEY = key$i END")
        }

        val keys = keyRange.map(i => s"key$i").toVector

        val dm = cache.getAdvancedCache.getDistributionManager
        val self = cache.getAdvancedCache.getRpcManager.getAddress

        val pickNotAssignedSelfKey = keys.find(key => !dm.getLocality(key).isLocal)

        pickNotAssignedSelfKey should not be(None)

        pickNotAssignedSelfKey.foreach { key =>
          println(s"===== NOT ASSIGNED KEY = $key START =====")
          cache.get(key) should be(key.replace("key", "value"))
          println(s"===== NOT ASSIGNED KEY = $key END =====")

          println(s"===== RE LOOKUP KEY = $key START =====")
          cache.get(key) should be(key.replace("key", "value"))
          println(s"===== RE LOOKUP KEY = $key END =====")
        }
      }
    }
  }

  protected def withCache[K, V](cacheName: String, numInstances: Int = 1)(fun: Cache[K, V] => Unit): Unit = {
    val managers = (1 to numInstances).map(_ => new DefaultCacheManager("infinispan.xml"))
    managers.foreach(_.getCache[K, V](cacheName))

    try {
      val cache = managers(0).getCache[K, V](cacheName)
      fun(cache)
    } finally {
      managers.foreach(_.getCache[K, V](cacheName).stop())
      managers.foreach(_.stop())
    }
  }
}
