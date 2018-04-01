package org.littlewings.infinispan.evictionstrategy

import org.infinispan.Cache
import org.infinispan.commons.CacheException
import org.infinispan.configuration.cache.StorageType
import org.infinispan.eviction.EvictionStrategy
import org.infinispan.interceptors.impl.ContainerFullException
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.remoting.RemoteException
import org.scalatest.{FunSuite, Matchers}

class EvictionStrategySuite extends FunSuite with Matchers {
  test("default eviction strategy") {
    withCache[String, String]("defaultEvictionStrategyCache", 3) { cache =>
      val configuration = cache.getCacheConfiguration
      configuration.memory.evictionStrategy should be(EvictionStrategy.NONE)
      configuration.memory.storageType should be(StorageType.OBJECT)

      (1 to 50).foreach(i => cache.put(s"key${i}", s"value${i}"))

      cache.size should be(50L)

      println("size = " + cache.size)

      cache.forEach((key, _) => cache.evict(key))
      cache.forEach((key, _) => println(key))

      println("size = " + cache.size)

      cache.size should (be >= 33 or be <= 34)
    }
  }

  test("none eviction strategy, but select remove strategy") {
    withCache[String, String]("noneEvictionStrategyCache", 3) { cache =>
      // REMOVE!!
      cache.getCacheConfiguration.memory.evictionStrategy should be(EvictionStrategy.REMOVE)
      cache.getCacheConfiguration.memory.size should be(10L)

      (1 to 50).foreach(i => cache.put(s"key${i}", s"value${i}"))

      cache.size should be(30L)

      println("size = " + cache.size)

      cache.forEach((key, _) => cache.evict(key))
      cache.forEach((key, _) => println(key))

      println("size = " + cache.size)

      cache.size should be(20L)
    }
  }

  test("manual eviction strategy, but select remove strategy") {
    withCache[String, String]("manualEvictionStrategyCache", 3) { cache =>
      // REMOVE!!
      cache.getCacheConfiguration.memory.evictionStrategy should be(EvictionStrategy.REMOVE)
      cache.getCacheConfiguration.memory.size should be(10L)

      (1 to 50).foreach(i => cache.put(s"key${i}", s"value${i}"))

      cache.size should be(30L)

      println("size = " + cache.size)

      cache.forEach((key, _) => cache.evict(key))
      cache.forEach((key, _) => println(key))

      println("size = " + cache.size)

      cache.size should be(20L)
    }
  }

  test("remove eviction strategy") {
    withCache[String, String]("removeEvictionStrategyCache", 3) { cache =>
      cache.getCacheConfiguration.memory.evictionStrategy should be(EvictionStrategy.REMOVE)
      cache.getCacheConfiguration.memory.size should be(10L)

      (1 to 50).foreach(i => cache.put(s"key${i}", s"value${i}"))

      cache.size should be(30L)

      println("size = " + cache.size)

      cache.forEach((key, _) => cache.evict(key))
      cache.forEach((key, _) => println(key))

      println("size = " + cache.size)

      cache.size should be(20L)
    }
  }

  test("exception eviction strategy") {
    withCache[String, String]("exceptionEvictionStrategyCache", 3) { cache =>
      cache.getCacheConfiguration.memory.evictionStrategy should be(EvictionStrategy.EXCEPTION)
      cache.getCacheConfiguration.memory.size should be(10L)

      val thrown = the[CacheException] thrownBy (1 to 50).foreach(i => cache.put(s"key${i}", s"value${i}"))
      thrown.getMessage should be("Could not commit implicit transaction")

      thrown.getCause.getCause.getCause match {
        case cause: RemoteException =>
          cause.getMessage should include("ISPN000217: Received exception from")

          val remoteCause = cause.getCause
          remoteCause should be(a[ContainerFullException])
          remoteCause.getMessage should be("ISPN000514: Container eviction limit 10 reached, write operation(s) is blocked")
        case cause: ContainerFullException =>
          cause.getMessage should be("ISPN000514: Container eviction limit 10 reached, write operation(s) is blocked")
        case _ =>
          fail(s"unknown Exception[${thrown.getCause}]")
      }

      cache.size should (be >=24 or be <= 30)

      println("size = " + cache.size)

      cache.forEach((key, _) => cache.evict(key))
      cache.forEach((key, _) => println(cache.get(key)))

      println("size = " + cache.size)

      cache.size should (be >= 14 or be <= 20)
    }
  }

  protected def withCache[K, V](cacheName: String, numInstances: Int = 3)(fun: Cache[K, V] => Unit): Unit = {
    val managers = (1 to numInstances).map(_ => new DefaultCacheManager("infinispan.xml"))

    managers.foreach(_.startCache(cacheName))

    try {
      val cache = managers(0).getCache[K, V](cacheName)
      fun(cache)
      cache.stop()
    } finally {
      managers.foreach(_.stop())
    }
  }
}
