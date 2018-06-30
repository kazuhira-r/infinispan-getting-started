package org.littlewings.infinispan.transaction

import java.util.concurrent.{CompletableFuture, Executors}

import org.infinispan.client.hotrod.configuration.{ConfigurationBuilder, TransactionMode}
import org.infinispan.client.hotrod.exceptions.HotRodClientException
import org.infinispan.client.hotrod.transaction.manager.RemoteTransactionManager
import org.infinispan.client.hotrod.{RemoteCache, RemoteCacheManager}
import org.scalatest.{FunSuite, Matchers}

class HotRodTransactionSpec extends FunSuite with Matchers {
  test("non-transactional-cache / mode none") {
    withCache[String, String]("default", TransactionMode.NONE) { cache =>
      cache.put("key1", "value1")
      cache.get("key1") should be("value1")

      val tm = cache.getTransactionManager
      tm should be(null)
    }
  }

  test("non-transactional-cache / mode non-durable-xa") {
    withCache[String, String]("default", TransactionMode.NON_DURABLE_XA) { cache =>
      cache.put("key1", "value1")
      cache.get("key1") should be("value1")

      val tm = cache.getTransactionManager

      tm should not be (null)

      tm.begin()

      val thrown = the[HotRodClientException] thrownBy cache.put("key2", "value2")
      thrown.getMessage should be("ISPN004084: Cache default doesn't support transactions. Please check the documentation how to configure it properly.")

      tm.commit()
    }
  }

  test("non-xa-transactional-cache / mode non-xa") {
    withCache[String, String]("nonXaCache", TransactionMode.NON_XA) { cache =>
      val tm = cache.getTransactionManager

      tm.begin()

      cache.put("key1", "value1")
      cache.get("key1") should be("value1")

      tm.commit()

      cache.get("key1") should be("value1")


      tm.begin()

      cache.put("key2", "value2")
      cache.get("key2") should be("value2")

      tm.rollback()

      cache.get("key2") should be(null)
    }
  }

  test("non-xa-transactional-cache / mode non-durable-xa") {
    withCache[String, String]("nonXaCache", TransactionMode.NON_DURABLE_XA) { cache =>
      val tm = cache.getTransactionManager

      tm.begin()

      cache.put("key3", "value3")
      cache.get("key3") should be("value3")

      tm.commit()

      cache.get("key3") should be("value3")


      tm.begin()

      cache.put("key4", "value4")
      cache.get("key4") should be("value4")

      tm.rollback()

      cache.get("key4") should be(null)
    }
  }

  test("non-durable-xa-transactional-cache / mode non-xa") {
    withCache[String, String]("nonDurableXaCache", TransactionMode.NON_XA) { cache =>
      val tm = cache.getTransactionManager

      tm.begin()

      cache.put("key1", "value1")
      cache.get("key1") should be("value1")

      tm.commit()

      cache.get("key1") should be("value1")


      tm.begin()

      cache.put("key2", "value2")
      cache.get("key2") should be("value2")

      tm.rollback()

      cache.get("key2") should be(null)
    }
  }

  test("non-durable-xa-transactional-cache / mode non-xa / override non-durable-xa") {
    withCacheManager(TransactionMode.NON_XA) { manager =>
      val cache = manager.getCache[String, String]("nonDurableXaCache", TransactionMode.NON_DURABLE_XA)

      val tm = cache.getTransactionManager

      tm.begin()

      cache.put("key3", "value3")
      cache.get("key3") should be("value3")

      tm.commit()

      cache.get("key3") should be("value3")


      tm.begin()

      cache.put("key4", "value4")
      cache.get("key4") should be("value4")

      tm.rollback()

      cache.get("key4") should be(null)
    }
  }

  test("non-xa-transactional-cache / mode full-xa") {
    withCacheManager(TransactionMode.FULL_XA) { manager =>
      val thrown = the[IllegalArgumentException] thrownBy manager.getCache[String, String]("nonXaCache")
      thrown.getMessage should be("FULL_XA isn't supported yet!")
    }
  }

  test("transaction-manager implementation") {
    withCache[String, String]("nonXaCache", TransactionMode.NON_XA) { cache =>
      val tm = cache.getTransactionManager
      tm should be(a[RemoteTransactionManager])
    }
  }

  test("non-xa-transactional-cache / mode non-xa / multiple cache") {
    withCacheManager(TransactionMode.NON_XA) { manager =>
      val cache1 = manager.getCache[String, String]("nonXaCache1")
      val cache2 = manager.getCache[String, String]("nonXaCache2")

      val tm = cache1.getTransactionManager

      tm.begin()

      cache1.put("key1-1", "value1-1")
      cache2.put("key2-1", "value2-1")

      cache1.get("key1-1") should be("value1-1")
      cache2.get("key2-1") should be("value2-1")

      tm.commit()

      cache1.get("key1-1") should be("value1-1")
      cache2.get("key2-1") should be("value2-1")


      tm.begin()

      cache1.put("key1-2", "value1-2")
      cache2.put("key2-2", "value2-2")

      cache1.get("key1-2") should be("value1-2")
      cache2.get("key2-2") should be("value2-2")

      tm.rollback()

      cache1.get("key1-2") should be(null)
      cache2.get("key2-2") should be(null)
    }
  }

  test("non-durable-xa-transactional-cache / mode non-durable-xa / multiple cache") {
    withCacheManager(TransactionMode.NON_DURABLE_XA) { manager =>
      val cache1 = manager.getCache[String, String]("nonDurableXaCache1")
      val cache2 = manager.getCache[String, String]("nonDurableXaCache2")

      val tm = cache1.getTransactionManager

      tm.begin()

      cache1.put("key1-1", "value1-1")
      cache2.put("key2-1", "value2-1")

      cache1.get("key1-1") should be("value1-1")
      cache2.get("key2-1") should be("value2-1")

      tm.commit()

      cache1.get("key1-1") should be("value1-1")
      cache2.get("key2-1") should be("value2-1")


      tm.begin()

      cache1.put("key1-2", "value1-2")
      cache2.put("key2-2", "value2-2")

      cache1.get("key1-2") should be("value1-2")
      cache2.get("key2-2") should be("value2-2")

      tm.rollback()

      cache1.get("key1-2") should be(null)
      cache2.get("key2-2") should be(null)
    }
  }

  test("transaction visibility / non-xa-transactional-cache / mode non-xa") {
    withCache[String, String]("nonXaCache", TransactionMode.NON_XA) { cache =>
      cache.clear()

      cache.put("key10", "value10") // initial
      cache.put("key30", "value30") // initial

      val tm = cache.getTransactionManager

      val updateExecutor = Executors.newSingleThreadExecutor
      val readExecutor = Executors.newSingleThreadExecutor

      val future =
        CompletableFuture
          .runAsync(() => tm.begin(), updateExecutor)
          .thenRunAsync(() => tm.begin(), readExecutor)

          .thenRunAsync(() => cache.put("key20", "value20"), updateExecutor) // insert
          .thenRunAsync(() => cache.get("key20") should be(null), readExecutor) // reader, non-visible

          .thenRunAsync(() => cache.put("key10", "value10-1"), updateExecutor) // update
          .thenRunAsync(() => cache.get("key10") should be("value10"), readExecutor) // reader, still-old-value-visible

          .thenRunAsync(() => cache.remove("key30"), updateExecutor) // delete
          .thenRunAsync(() => cache.get("key30") should be("value30"), readExecutor) // reader, still-visible

          .thenRunAsync(() => tm.commit(), updateExecutor)

          .thenRunAsync(() => cache.get("key20") should be(null), readExecutor) // reader, non-visible
          .thenRunAsync(() => cache.get("key10") should be("value10"), readExecutor) // reader, still-old-value-visible
          .thenRunAsync(() => cache.get("key30") should be("value30"), readExecutor) // reader, still-visible

          .thenRunAsync(() => tm.commit(), readExecutor)

          .thenRunAsync(() => cache.get("key20") should be("value20"), updateExecutor)
          .thenRunAsync(() => cache.get("key10") should be("value10-1"), updateExecutor)
          .thenRunAsync(() => cache.get("key30") should be(null), updateExecutor)

          .thenRunAsync(() => cache.get("key20") should be("value20"), readExecutor) // reader, visible
          .thenRunAsync(() => cache.get("key10") should be("value10-1"), readExecutor) // reader, visible
          .thenRunAsync(() => cache.get("key30") should be(null), readExecutor) // reader, deleted

      future.join()

      cache.get("key20") should be("value20")
      cache.get("key10") should be("value10-1")
      cache.get("key30") should be(null)
    }
  }

  test("transaction visibility / non-durable-xa-transactional-cache / mode non-durable-xa") {
    withCache[String, String]("nonDurableXaCache", TransactionMode.NON_DURABLE_XA) { cache =>
      cache.clear()

      cache.put("key10", "value10") // initial
      cache.put("key30", "value30") // initial

      val tm = cache.getTransactionManager

      val updateExecutor = Executors.newSingleThreadExecutor
      val readExecutor = Executors.newSingleThreadExecutor

      val future =
        CompletableFuture
          .runAsync(() => tm.begin(), updateExecutor)
          .thenRunAsync(() => tm.begin(), readExecutor)

          .thenRunAsync(() => cache.put("key20", "value20"), updateExecutor) // insert
          .thenRunAsync(() => cache.get("key20") should be(null), readExecutor) // reader, non-visible

          .thenRunAsync(() => cache.put("key10", "value10-1"), updateExecutor) // update
          .thenRunAsync(() => cache.get("key10") should be("value10"), readExecutor) // reader, still-old-value-visible

          .thenRunAsync(() => cache.remove("key30"), updateExecutor) // delete
          .thenRunAsync(() => cache.get("key30") should be("value30"), readExecutor) // reader, still-visible

          .thenRunAsync(() => tm.commit(), updateExecutor)

          .thenRunAsync(() => cache.get("key20") should be(null), readExecutor) // reader, non-visible
          .thenRunAsync(() => cache.get("key10") should be("value10"), readExecutor) // reader, still-old-value-visible
          .thenRunAsync(() => cache.get("key30") should be("value30"), readExecutor) // reader, still-visible

          .thenRunAsync(() => tm.commit(), readExecutor)

          .thenRunAsync(() => cache.get("key20") should be("value20"), updateExecutor)
          .thenRunAsync(() => cache.get("key10") should be("value10-1"), updateExecutor)
          .thenRunAsync(() => cache.get("key30") should be(null), updateExecutor)

          .thenRunAsync(() => cache.get("key20") should be("value20"), readExecutor) // reader, visible
          .thenRunAsync(() => cache.get("key10") should be("value10-1"), readExecutor) // reader, visible
          .thenRunAsync(() => cache.get("key30") should be(null), readExecutor) // reader, deleted

      future.join()

      cache.get("key20") should be("value20")
      cache.get("key10") should be("value10-1")
      cache.get("key30") should be(null)
    }
  }

  test("conflict / non-xa-transactional-cache / mode non-xa") {
    withCache[String, String]("nonXaCache", TransactionMode.NON_XA) { cache =>
      cache.clear()

      cache.put("key10", "value10") // initial

      val tm = cache.getTransactionManager

      val firstUpdateExecutor = Executors.newSingleThreadExecutor
      val secondUpdateExecutor = Executors.newSingleThreadExecutor

      val future =
        CompletableFuture
          .runAsync(() => tm.begin(), firstUpdateExecutor)
          .thenRunAsync(() => tm.begin(), secondUpdateExecutor)

          .thenRunAsync(() => cache.put("key10", "value10-1-1"), firstUpdateExecutor)
          .thenRunAsync(() => cache.put("key10", "value10-2-1"), secondUpdateExecutor)

          .thenRunAsync(() => cache.get("key10") should be("value10-1-1"), firstUpdateExecutor)
          .thenRunAsync(() => cache.get("key10") should be("value10-2-1"), secondUpdateExecutor)

          .thenRunAsync(() => tm.commit(), firstUpdateExecutor)
          .thenRunAsync(() => tm.commit(), secondUpdateExecutor)

          .thenRunAsync(() => cache.get("key10") should be("value10-2-1"), firstUpdateExecutor)  // last updated
          .thenRunAsync(() => cache.get("key10") should be("value10-2-1"), secondUpdateExecutor)


      future.join()

      cache.get("key10") should be("value10-2-1")  // last updated
    }
  }

  test("conflict / non-durable-xa-transactional-cache / mode non-durable-xa") {
    withCache[String, String]("nonDurableXaCache", TransactionMode.NON_DURABLE_XA) { cache =>
      cache.clear()

      cache.put("key10", "value10") // initial

      val tm = cache.getTransactionManager

      val firstUpdateExecutor = Executors.newSingleThreadExecutor
      val secondUpdateExecutor = Executors.newSingleThreadExecutor

      val future =
        CompletableFuture
          .runAsync(() => tm.begin(), firstUpdateExecutor)
          .thenRunAsync(() => tm.begin(), secondUpdateExecutor)

          .thenRunAsync(() => cache.put("key10", "value10-1-1"), firstUpdateExecutor)
          .thenRunAsync(() => cache.put("key10", "value10-2-1"), secondUpdateExecutor)

          .thenRunAsync(() => cache.get("key10") should be("value10-1-1"), firstUpdateExecutor)
          .thenRunAsync(() => cache.get("key10") should be("value10-2-1"), secondUpdateExecutor)

          .thenRunAsync(() => tm.commit(), firstUpdateExecutor)
          .thenRunAsync(() => tm.commit(), secondUpdateExecutor)

          .thenRunAsync(() => cache.get("key10") should be("value10-2-1"), firstUpdateExecutor)  // last updated
          .thenRunAsync(() => cache.get("key10") should be("value10-2-1"), secondUpdateExecutor)


      future.join()

      cache.get("key10") should be("value10-2-1")  // last updated
    }
  }

  protected def withCacheManager(transactionMode: TransactionMode = TransactionMode.NONE)(fun: RemoteCacheManager => Unit): Unit = {
    val manager =
      new RemoteCacheManager(
        new ConfigurationBuilder()
          .addServer()
          .host("172.17.0.2")
          .port(11222)
          .transaction()
          .transactionMode(transactionMode)
          .build()
      )

    try {
      fun(manager)
    } finally {
      manager.stop()
    }
  }

  protected def withCache[K, V](cacheName: String, transactionMode: TransactionMode = TransactionMode.NONE)(fun: RemoteCache[K, V] => Unit): Unit =
    withCacheManager(transactionMode) { manager =>
      val cache = manager.getCache[K, V](cacheName)
      fun(cache)
      cache.stop()
    }
}
