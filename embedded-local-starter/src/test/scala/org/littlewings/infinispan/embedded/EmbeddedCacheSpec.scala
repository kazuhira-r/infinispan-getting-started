package org.littlewings.infinsipan.embedded

import java.util.concurrent.TimeUnit

import org.infinispan.configuration.cache.ConfigurationBuilder
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.transaction.{ LockingMode, TransactionMode }
import org.infinispan.util.concurrent.IsolationLevel

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class EmbeddedCacheSpec extends FunSpec {
  describe("Infinispan Embedded Cache, Gettings Started") {
    it("non configuration Cache") {
      val manager = new DefaultCacheManager
      val cache = manager.getCache[String, String]

      cache.put("key1", "value1")
      cache.put("key2", "value2")

      cache.get("key1") should be ("value1")
      cache.get("key2") should be ("value2")
      cache.get("not-exists-key") should be (null)

      cache.remove("key1")
      cache.remove("key2")

      cache.get("key1") should be (null)
      cache.get("key2") should be (null)

      cache.stop()
      manager.stop()
    }

    it("using Named Cache, non configuration") {
      val manager = new DefaultCacheManager
      val cache = manager.getCache[String, String]("namedCache")

      cache.put("key1", "value1")
      cache.get("key1") should be ("value1")

      cache.getName should be ("namedCache")

      cache.stop()
      manager.stop()
    }

    it("confirm expiration, define programattically") {
      val manager = new DefaultCacheManager
      manager.defineConfiguration("withExpireCache",
        new ConfigurationBuilder()
          .expiration
          .maxIdle(3000L)
          .lifespan(5000L)
          .build)

      val cache = manager.getCache[String, String]("withExpireCache")

      cache.put("key1", "value1")
      cache.put("key2", "value2")

      TimeUnit.SECONDS.sleep(2)

      cache.get("key1") should be ("value1")

      TimeUnit.SECONDS.sleep(2)

      // maxIdle
      cache.get("key1") should be ("value1")
      cache.get("key2") should be (null)

      TimeUnit.SECONDS.sleep(2)

      // lifespan
      cache.get("key1") should be (null)
      cache.get("key2") should be (null)

      cache.stop()
      manager.stop()
    }

    it("confirm expiration, define configuration-file") {
      val manager = new DefaultCacheManager("infinispan.xml")
      val cache = manager.getCache[String, String]("withExpireCache")

      cache.put("key1", "value1")
      cache.put("key2", "value2")

      TimeUnit.SECONDS.sleep(2)

      cache.get("key1") should be ("value1")

      TimeUnit.SECONDS.sleep(2)

      // maxIdle
      cache.get("key1") should be ("value1")
      cache.get("key2") should be (null)

      TimeUnit.SECONDS.sleep(2)

      // lifespan
      cache.get("key1") should be (null)
      cache.get("key2") should be (null)

      cache.stop()
      manager.stop()
    }

    it("define configuration cache") {
      val manager = new DefaultCacheManager("infinispan.xml")

      val defaultCache = manager.getCache[String, String]
      val transactionalCache = manager.getCache[String, String]("transactionalCache")
      val nonDefinedCache = manager.getCache[String, String]("nonDefinedCache")

      defaultCache.getCacheConfiguration.expiration.lifespan should be (5000)
      defaultCache.getCacheConfiguration.expiration.maxIdle should be (3000)

      // extends default cache
      transactionalCache.getCacheConfiguration.expiration.lifespan should be (5000)
      transactionalCache.getCacheConfiguration.expiration.maxIdle should be (3000)
      // specified configuration
      transactionalCache
        .getCacheConfiguration
        .locking
        .isolationLevel should be (IsolationLevel.REPEATABLE_READ)
      transactionalCache
        .getCacheConfiguration
        .transaction
        .transactionMode should be (TransactionMode.TRANSACTIONAL)
      transactionalCache
        .getCacheConfiguration
        .transaction
        .lockingMode should be (LockingMode.OPTIMISTIC)

      nonDefinedCache.getCacheConfiguration.expiration.lifespan should be (5000)
      nonDefinedCache.getCacheConfiguration.expiration.maxIdle should be (3000)

      defaultCache.stop()
      transactionalCache.stop()
      nonDefinedCache.stop()

      manager.stop()
    }
  }
}
