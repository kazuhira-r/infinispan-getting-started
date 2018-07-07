package org.littlewings.infinispan.writebehind

import java.util.concurrent.TimeUnit

import org.infinispan.Cache
import org.infinispan.configuration.cache.{AsyncStoreConfigurationBuilder, ConfigurationBuilder}
import org.infinispan.manager.DefaultCacheManager
import org.jboss.logging.Logger
import org.scalatest.{FunSuite, Matchers}

class FaultTolerantCacheStoreSpec extends FunSuite with Matchers {
  val logger: Logger = Logger.getLogger(getClass)

  test("write-behind store, simply") {
    withCache[String, String](
      "write-behind",
      configuration =>
        configuration
          .persistence
          .addStore(classOf[InMemoryCacheStoreConfigurationBuilder])
          .async.enable // write-behind enabled
    ) { cache =>
      InMemoryCacheStore.COUNTER.set(0)

      cache.put("key1", "value1")
      cache.put("key2", "value2")
      cache.put("key3", "value3")

      TimeUnit.SECONDS.sleep(1L)

      InMemoryCacheStore.currentStoreEntries[String, String] should contain only(
        "key1" -> "value1", "key2" -> "value2", "key3" -> "value3"
      )
    }
  }

  test("write-behind store, write failed") {
    withCache[String, String](
      "write-behind",
      configuration =>
        configuration
          .persistence
          .connectionAttempts(2) // retry-count
          .availabilityInterval(1000) // store-state polling-interval (default)
          .addStore(classOf[InMemoryCacheStoreConfigurationBuilder])
          .async.enable // write-behind enabled
    ) { cache =>
      InMemoryCacheStore.COUNTER.set(3)

      cache.put("key1", "value1")
      cache.put("key2", "value2")
      cache.put("key3", "value3")

      TimeUnit.SECONDS.sleep(6L) // waiting, all retry failed...

      InMemoryCacheStore.currentStoreEntries[String, String] should be(empty)
    }
  }

  test("write-behind store, fault-tolerant, simply") {
    withCache[String, String](
      "write-behind",
      configuration =>
        configuration
          .persistence
          .connectionAttempts(3) // retry-count
          .availabilityInterval(1000) // store-state polling-interval (default)
          .addStore(classOf[InMemoryCacheStoreConfigurationBuilder])
          .async.enable // write-behind enabled
    ) { cache =>
      InMemoryCacheStore.COUNTER.set(2)

      cache.put("key1", "value1")
      cache.put("key2", "value2")
      cache.put("key3", "value3")

      TimeUnit.SECONDS.sleep(1L)

      logger.infof("set store availability = false")
      InMemoryCacheStore.AVAILABLE.set(false)

      TimeUnit.SECONDS.sleep(3L)

      logger.infof("set store availability = true")
      InMemoryCacheStore.AVAILABLE.set(true)

      TimeUnit.SECONDS.sleep(5L)

      InMemoryCacheStore.currentStoreEntries[String, String] should contain only(
        "key1" -> "value1", "key2" -> "value2", "key3" -> "value3"
      )
    }
  }

  test("write-behind store, fault-tolerant, retry-continue?") {
    withCache[String, String](
      "write-behind",
      configuration =>
        configuration
          .persistence
          .connectionAttempts(10) // retry-count
          .availabilityInterval(1000) // store-state polling-interval (default)
          .addStore(classOf[InMemoryCacheStoreConfigurationBuilder])
          .async.enable // write-behind enabled
    ) { cache =>
      InMemoryCacheStore.COUNTER.set(8)

      cache.put("key1", "value1")

      TimeUnit.SECONDS.sleep(3L)

      // CacheWriterへの書き込み停止
      logger.infof("set store availability = false")
      InMemoryCacheStore.AVAILABLE.set(false)

      TimeUnit.SECONDS.sleep(3L)

      // CacheWriterへの書き込み再開（が、失敗し続ける（リトライ回数は継続））
      logger.infof("set store availability = true")
      InMemoryCacheStore.AVAILABLE.set(true)

      TimeUnit.SECONDS.sleep(3L)

      // CacheWriterへの書き込み停止
      logger.infof("set store availability = false")
      InMemoryCacheStore.AVAILABLE.set(false)

      cache.put("key2", "value2")

      TimeUnit.SECONDS.sleep(3L)

      // CacheWriterへの書き込み再開（書き込みは成功するようにカウンタリセット）
      InMemoryCacheStore.COUNTER.set(0)
      logger.infof("set store availability = true")
      InMemoryCacheStore.AVAILABLE.set(true)

      cache.put("key3", "value3")
      TimeUnit.SECONDS.sleep(3L)

      InMemoryCacheStore.currentStoreEntries[String, String] should contain only(
        "key1" -> "value1", "key2" -> "value2", "key3" -> "value3"
      )
    }
  }

  test("write-behind store, fault-tolerant, duplicate") {
    withCache[String, String](
      "write-behind",
      configuration =>
        configuration
          .persistence
          .connectionAttempts(5) // retry-count
          .availabilityInterval(1000) // store-state polling-interval (default)
          .addStore(classOf[InMemoryCacheStoreConfigurationBuilder])
          .async.enable // write-behind enabled
    ) { cache =>
      InMemoryCacheStore.COUNTER.set(4)

      cache.put("key1", "value1")
      cache.put("key2", "value2")
      cache.put("key3", "value3")

      TimeUnit.SECONDS.sleep(2L)

      cache.put("key1", "value1-1")
      cache.put("key2", "value2-2")
      cache.put("key3", "value3-3")

      TimeUnit.SECONDS.sleep(1L)


      TimeUnit.SECONDS.sleep(1L)

      logger.infof("set store availability = false")
      InMemoryCacheStore.AVAILABLE.set(false)

      TimeUnit.SECONDS.sleep(1L)

      InMemoryCacheStore.COUNTER.set(0)

      logger.infof("set store availability = true")
      InMemoryCacheStore.AVAILABLE.set(true)

      TimeUnit.SECONDS.sleep(6L)

      InMemoryCacheStore.currentStoreEntries[String, String] should contain only(
        "key1" -> "value1-1", "key2" -> "value2-2", "key3" -> "value3-3"
      )
    }
  }

  test("write-behind store, fault-tolerant, disabled") {
    withCache[String, String](
      "write-behind",
      configuration =>
        configuration
          .persistence
          .connectionAttempts(3) // retry-count
          .availabilityInterval(1000) // store-state polling-interval (default)
          .addStore(classOf[InMemoryCacheStoreConfigurationBuilder])
          .async.enable // write-behind enabled
          .failSilently(true)  // default false
    ) { cache =>
      InMemoryCacheStore.COUNTER.set(3)

      logger.infof("set store availability = false")
      InMemoryCacheStore.AVAILABLE.set(false)

      cache.put("key1", "value1")
      cache.put("key2", "value2")
      cache.put("key3", "value3")

      TimeUnit.SECONDS.sleep(2L)

      logger.infof("set store availability = true")
      InMemoryCacheStore.AVAILABLE.set(true)

      InMemoryCacheStore.currentStoreEntries[String, String] should be(empty)
    }
  }

  protected def withCache[K, V](cacheName: String, persistenceBuilder: ConfigurationBuilder => AsyncStoreConfigurationBuilder[InMemoryCacheStoreConfigurationBuilder])(fun: Cache[K, V] => Unit): Unit = {
    val configuration = persistenceBuilder(new ConfigurationBuilder).build()

    val manager = new DefaultCacheManager
    manager.defineConfiguration(cacheName, configuration)

    try {
      val cache = manager.getCache[K, V](cacheName)
      fun(cache)
      cache.stop()
    } finally {
      manager.stop()
    }
  }
}
