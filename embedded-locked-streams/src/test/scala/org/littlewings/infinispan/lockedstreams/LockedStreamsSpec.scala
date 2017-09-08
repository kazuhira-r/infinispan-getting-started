package org.littlewings.infinispan.lockedstreams

import org.infinispan.Cache
import org.infinispan.container.entries.CacheEntry
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.util.function.{SerializableBiConsumer, SerializableConsumer, SerializablePredicate}
import org.scalatest.{FunSuite, Matchers}

class LockedStreamsSpec extends FunSuite with Matchers {
  test("simple locked-streams") {
    withCache[String, Integer]("distributedCache", 3) { cache =>
      (1 to 10).foreach(i => cache.put(s"key${i}", i))

      val lockedStream = cache.getAdvancedCache.lockedStream

      try {
        lockedStream
          .filter(new SerializablePredicate[CacheEntry[String, Integer]] {
            override def test(e: CacheEntry[String, Integer]): Boolean = e.getValue % 2 == 0
          })
          .forEach(new SerializableBiConsumer[Cache[String, Integer], CacheEntry[String, Integer]] {
            override def accept(c: Cache[String, Integer], entry: CacheEntry[String, Integer]): Unit =
              println(s"Cache[${c.getName}]: entry[${entry}]")
          })
      } finally {
        lockedStream.close()
      }
    }
  }

  test("optimistic-lock cache, not supported") {
    withCache[String, Integer]("withOptimisticLockCache", 3) { cache =>
      val transactionManager = cache.getAdvancedCache.getTransactionManager
      transactionManager.begin()
      (1 to 10).foreach(i => cache.put(s"key${i}", i))
      transactionManager.commit()

      val thrown = the[UnsupportedOperationException] thrownBy cache.getAdvancedCache.lockedStream
      thrown.getMessage should be("Method lockedStream is not supported in OPTIMISTIC transactional caches!")
    }
  }

  test("with pessimistic-lock, locked-streams") {
    withCache[String, Integer]("withPessimisticLockCache", 3) { cache =>
      val transactionManager = cache.getAdvancedCache.getTransactionManager
      transactionManager.begin()
      (1 to 10).foreach(i => cache.put(s"key${i}", i))
      transactionManager.commit()

      val lockedStream = cache.getAdvancedCache.lockedStream

      try {
        lockedStream
          .filter(new SerializablePredicate[CacheEntry[String, Integer]] {
            override def test(e: CacheEntry[String, Integer]): Boolean = e.getValue % 2 == 0
          })
          .forEach(new SerializableBiConsumer[Cache[String, Integer], CacheEntry[String, Integer]] {
            override def accept(c: Cache[String, Integer], entry: CacheEntry[String, Integer]): Unit =
              println(s"Cache[${c.getName}]: entry[${entry}]")
          })
      } finally {
        lockedStream.close()
      }
    }
  }

  test("simple locked-streams, lookup and update another cache entry") {
    withCache[String, Integer]("distributedCache", 3) { cache =>
      (1 to 10).foreach(i => cache.put(s"key${i}", i))

      val lockedStream = cache.getAdvancedCache.lockedStream

      try {
        lockedStream
          .filter(new SerializablePredicate[CacheEntry[String, Integer]] {
            override def test(e: CacheEntry[String, Integer]): Boolean = e.getValue % 2 == 0
          })
          .forEach(new SerializableBiConsumer[Cache[String, Integer], CacheEntry[String, Integer]] {
            override def accept(c: Cache[String, Integer], entry: CacheEntry[String, Integer]): Unit = {
              val key = entry.getKey
              val value = entry.getValue
              val keyAsInt = key.replace("key", "").toInt
              val nextKey = if (keyAsInt >= 10) "key1" else s"key${keyAsInt + 1}"

              val nextValue = c.get(nextKey)

              c.put(key, nextValue + value)

            }
          })
      } finally {
        lockedStream.close()
      }

      val stream = cache.entrySet().stream

      try {
        stream.forEach(new SerializableConsumer[java.util.Map.Entry[String, Integer]] {
          override def accept(e: java.util.Map.Entry[String, Integer]): Unit =
            println(s"updated: key[${e.getKey}], value[${e.getValue}]")
        })
      } finally {
        stream.close()
      }
    }
  }

  test("with pessimistic-lock, locked-streams, lookup and update another cache entry") {
    withCache[String, Integer]("withPessimisticLockCache", 3) { cache =>
      val transactionManager = cache.getAdvancedCache.getTransactionManager
      transactionManager.begin()
      (1 to 10).foreach(i => cache.put(s"key${i}", i))
      transactionManager.commit()

      val lockedStream = cache.getAdvancedCache.lockedStream

      try {
        lockedStream
          .filter(new SerializablePredicate[CacheEntry[String, Integer]] {
            override def test(e: CacheEntry[String, Integer]): Boolean = e.getValue % 2 == 0
          })
          .forEach(new SerializableBiConsumer[Cache[String, Integer], CacheEntry[String, Integer]] {
            override def accept(c: Cache[String, Integer], entry: CacheEntry[String, Integer]): Unit = {
              val tm = c.getAdvancedCache.getTransactionManager
              tm.begin()

              val key = entry.getKey
              val value = entry.getValue
              val keyAsInt = key.replace("key", "").toInt
              val nextKey = if (keyAsInt >= 10) "key1" else s"key${keyAsInt + 1}"

              val nextValue = c.get(nextKey)

              c.put(key, nextValue + value)

              tm.commit()
            }
          })
      } finally {
        lockedStream.close()
      }

      val stream = cache.entrySet().stream

      try {
        stream.forEach(new SerializableConsumer[java.util.Map.Entry[String, Integer]] {
          override def accept(e: java.util.Map.Entry[String, Integer]): Unit =
            println(s"updated: key[${e.getKey}], value[${e.getValue}]")
        })
      } finally {
        stream.close()
      }
    }
  }

  protected def withCache[K, V](cacheName: String, numInstances: Int)(fun: Cache[K, V] => Unit): Unit = {
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
