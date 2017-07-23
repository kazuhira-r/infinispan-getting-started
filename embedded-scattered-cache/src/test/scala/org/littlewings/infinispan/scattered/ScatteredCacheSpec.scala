package org.littlewings.infinispan.scattered

import java.util.concurrent.TimeUnit

import org.infinispan.Cache
import org.infinispan.commons.util.Util
import org.infinispan.distribution.group.impl.PartitionerConsistentHash
import org.infinispan.manager.{DefaultCacheManager, EmbeddedCacheManager}
import org.infinispan.util.function.SerializableToIntFunction
import org.scalatest.{FunSuite, Matchers}

class ScatteredCacheSpec extends FunSuite with Matchers {
  test("simple use Scattered Cache") {
    withCache[String, String]("simpleScatteredCache", 3) { cache =>
      (1 to 10).foreach(i => cache.put(s"key$i", s"value$i"))

      (1 to 10).foreach(i => cache.get(s"key$i") should be(s"value$i"))

      cache should have size (10)
    }
  }

  test("Scattered Cache, default configuration") {
    withCache[String, String]("simpleScatteredCache", 3) { cache =>
      (1 to 10).foreach(i => cache.put(s"key$i", s"value$i"))

      val dm = cache.getAdvancedCache.getDistributionManager
      dm.getReadConsistentHash.toString should startWith("PartitionerConsistentHash:ScatteredConsistentHash{ns=")
      dm.getWriteConsistentHash.toString should startWith("PartitionerConsistentHash:ScatteredConsistentHash{ns=")

      dm.getReadConsistentHash.getNumSegments should be(256)
      dm.getWriteConsistentHash.getNumSegments should be(256)

      cache.getCacheConfiguration.clustering.hash.numOwners should be(1)

      val cacheTopology = dm.getCacheTopology

      (1 to 10).foreach { i =>
        val distributionInfo = cacheTopology.getDistribution(s"key$i")
        distributionInfo.writeOwners should have size (1)
        distributionInfo.writeBackups should be(empty)
      }
    }
  }

  test("Scattered Cache, node down") {
    withCacheWithManagers[String, String]("simpleScatteredCache", 3) { (cache, managers) =>
      (1 to 100).foreach(i => cache.put(s"key$i", s"value$i"))
      cache should have size (100)

      val anotherCache = managers(1).getCache[String, String]("simpleScatteredCache")
      anotherCache should have size (100)

      cache.getCacheManager.stop()
      TimeUnit.SECONDS.sleep(3L)
      anotherCache should have size (100)

      managers(2).stop()
      TimeUnit.SECONDS.sleep(3L)
      anotherCache should have size (100)

      anotherCache.stop()
    }
  }

  test("Distributed Cache, node down") {
    withCacheWithManagers[String, String]("simpleDistributedCache", 3) { (cache, managers) =>
      (1 to 100).foreach(i => cache.put(s"key$i", s"value$i"))
      cache should have size (100)

      val anotherCache = managers(1).getCache[String, String]("simpleDistributedCache")
      anotherCache should have size (100)

      cache.getCacheManager.stop()
      TimeUnit.SECONDS.sleep(3L)
      anotherCache should have size (100)

      managers(2).stop()
      TimeUnit.SECONDS.sleep(3L)
      anotherCache should have size (100)

      anotherCache.stop()
    }
  }

  test("Scattered Cache, data distribution") {
    withCache[String, String]("simpleScatteredCache", 3) { cache =>
      (1 to 10).foreach(i => cache.put(s"key$i", s"value$i"))

      val self = cache.getCacheManager.getAddress
      val dm = cache.getAdvancedCache.getDistributionManager
      val cacheTopology = dm.getCacheTopology

      println(s"self = $self")
      (1 to 10).foreach(i => println(cacheTopology.getDistribution(s"key$i").primary()))

      (1 to 10).foreach(i => cache.put(s"key$i", s"value2-$i"))

      println(s"self = $self")
      (1 to 10).foreach(i => println(cacheTopology.getDistribution(s"key$i").primary()))
    }
  }

  test("Scattered Cache, hash segment") {
    withCache[String, String]("simpleScatteredCache", 3) { cache =>
      (1 to 10).foreach(i => cache.put(s"key$i", s"value$i"))

      val dm = cache.getAdvancedCache.getDistributionManager
      val readConHash = dm.getReadConsistentHash.asInstanceOf[PartitionerConsistentHash]
      val writeConHash = dm.getWriteConsistentHash.asInstanceOf[PartitionerConsistentHash]

      val readSegmentSize = readConHash.getNumSegments
      val readHash = readConHash.getHashFunction
      val writeSegmentSize = writeConHash.getNumSegments
      val writeHash = writeConHash.getHashFunction

      (1 to 10).foreach { i =>
        val key = s"key$i"
        ((readHash.hash(key) & Integer.MAX_VALUE) / Util.getSegmentSize(readSegmentSize)) should be(readConHash.getSegment(key))
        ((writeHash.hash(key) & Integer.MAX_VALUE) / Util.getSegmentSize(writeSegmentSize)) should be(writeConHash.getSegment(key))
      }
    }
  }

  test("Scattered Cache, stream api") {
    withCache[String, String]("simpleScatteredCache", 3) { cache =>
      (1 to 10).foreach(i => cache.put(s"key$i", s"value$i"))

      val sum =
        cache
          .values
          .stream
          .mapToInt(new SerializableToIntFunction[String] {
            override def applyAsInt(value: String): Int =
              Integer.parseInt(value.replace("value", ""))
          })
          .sum

      sum should be(55)
    }
  }

  test("Scattered Cache, non key partitioner") {
    withCache[String, String]("simpleScatteredCache", 3) { cache =>
      (1 to 10).foreach { i =>
        val partition = (i % 3) + 1
        cache.put(s"key$partition-$i", s"value$partition-$i")
      }

      val dm = cache.getAdvancedCache.getDistributionManager
      val cacheTopology = dm.getCacheTopology

      val keys = cache.keySet

      keys.forEach { key =>
        println(s"key => $key")
        println(cacheTopology.getWriteOwners(key))
      }
    }
  }

  test("Scattered Cache, non grouper") {
    withCache[String, String]("simpleScatteredCache", 3) { cache =>
      (1 to 10).foreach { i =>
        val partition = (i % 3) + 1
        cache.put(s"key$partition-$i", s"value$partition-$i")
      }

      val dm = cache.getAdvancedCache.getDistributionManager
      val cacheTopology = dm.getCacheTopology

      val keys = cache.keySet

      keys.forEach { key =>
        println(s"key => $key")
        println(cacheTopology.getWriteOwners(key))
      }
    }
  }

  test("Scattered Cache, key partitioner") {
    withCache[String, String]("keyPartitionedScatteredCache", 3) { cache =>
      (1 to 10).foreach { i =>
        val partition = (i % 3) + 1
        cache.put(s"key$partition-$i", s"value$partition-$i")
      }

      val dm = cache.getAdvancedCache.getDistributionManager
      val cacheTopology = dm.getCacheTopology

      val keys = cache.keySet

      keys.forEach { key =>
        println(s"key => $key")
        println(cacheTopology.getWriteOwners(key))
      }
    }
  }

  test("Distributed Cache, key partitioner") {
    withCache[String, String]("keyPartitionedDistributedCache", 3) { cache =>
      (1 to 10).foreach { i =>
        val partition = (i % 3) + 1
        cache.put(s"key$partition-$i", s"value$partition-$i")
      }

      val dm = cache.getAdvancedCache.getDistributionManager
      val cacheTopology = dm.getCacheTopology

      val keys = cache.keySet

      keys.forEach { key =>
        println(s"key => $key")
        println(cacheTopology.getWriteOwners(key))
      }
    }
  }

  def withCache[K, V](cacheName: String, numInstances: Int = 1)(fun: Cache[K, V] => Unit): Unit = {
    val managers = (1 to numInstances).map(_ => new DefaultCacheManager("infinispan.xml"))

    managers.foreach(_.getCache[K, V](cacheName))

    try {
      val cache = managers(0).getCache[K, V](cacheName)

      fun(cache)
    } finally {
      managers.foreach(_.stop())
    }
  }

  def withCacheWithManagers[K, V](cacheName: String, numInstances: Int = 1)(fun: (Cache[K, V], IndexedSeq[EmbeddedCacheManager]) => Unit): Unit = {
    val managers = (1 to numInstances).map(_ => new DefaultCacheManager("infinispan.xml"))

    managers.foreach(_.getCache[K, V](cacheName))

    try {
      val cache = managers(0).getCache[K, V](cacheName)

      fun(cache, managers)
    } finally {
      managers.foreach(_.stop())
    }
  }
}
