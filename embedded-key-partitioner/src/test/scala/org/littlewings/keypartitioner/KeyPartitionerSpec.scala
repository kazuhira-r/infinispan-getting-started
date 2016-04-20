package org.littlewings.keypartitioner

import java.util.stream.Collectors

import org.infinispan.{Cache, CacheStream}
import org.infinispan.distribution.ch.impl.HashFunctionPartitioner
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.stream.CacheCollectors
import org.scalatest.{FunSpec, Matchers}

import scala.collection.JavaConverters._

class KeyPartitionerSpec extends FunSpec with Matchers {
  describe("Infinispan Key Partitioner") {
    it("default KeyPartitioner is HashFunctionPartitioner.") {
      withCache[String, String]("defaultDistributedCache", 3) { cache =>
        cache.getCacheConfiguration.clustering.hash.keyPartitioner should be(a[HashFunctionPartitioner])
      }
    }

    it("default segment size is 256.") {
      withCache[String, String]("defaultDistributedCache", 3) { cache =>
        cache.getCacheConfiguration.clustering.hash.numSegments should be(256)
      }
    }

    it("distributed hash partition.") {
      withCache[String, String]("defaultDistributedCache", 3) { cache =>
        for {
          i <- 1 to 10
          j <- 1 to 5
        } cache.put(s"key-${i}00-$j", s"value-${i}00-$j")

        val consistentHash = cache.getAdvancedCache.getDistributionManager.getConsistentHash
        val dm = cache.getAdvancedCache.getDistributionManager

        cache
          .keySet
          .asScala
          .toArray
          .sorted
          .foreach(key => println(s"key = ${key}, segment = ${consistentHash.getSegment(key)}, primaryOwner = ${dm.getPrimaryLocation(key)}"))
      }
    }

    it("using custom KeyPartitioner.") {
      withCache[String, String]("customPartitionerDistributedCache", 3) { cache =>
        cache.getCacheConfiguration.clustering.hash.keyPartitioner should be(a[MyKeyPartitioner])

        for {
          i <- 1 to 10
          j <- 1 to 5
        } cache.put(s"key-${i}00-$j", s"value-${i}00-$j")

        val consistentHash = cache.getAdvancedCache.getDistributionManager.getConsistentHash
        val dm = cache.getAdvancedCache.getDistributionManager

        cache
          .keySet
          .asScala
          .toArray
          .sorted
          .foreach(key => println(s"key = ${key}, segment = ${consistentHash.getSegment(key)}, primaryOwner = ${dm.getPrimaryLocation(key)}"))
      }
    }

    it("Distributed Streams, using custom KeyPartitioner.") {
      withCache[String, String]("customPartitionerDistributedCache", 3) { cache =>
        for {
          i <- 1 to 10
          j <- 1 to 5
        } cache.put(s"key-${i}00-$j", s"value-${i}00-$j")

        val consistentHash = cache.getAdvancedCache.getDistributionManager.getConsistentHash
        val dm = cache.getAdvancedCache.getDistributionManager

        val targetSegments =
          Array("key-100-1", "key-500-1", "key-900-1")
            .map(consistentHash.getSegment)
            .map(Integer.valueOf)
            .toSet

        val stream =
          cache.entrySet.stream.asInstanceOf[CacheStream[java.util.Map.Entry[String, String]]]

        val results =
          try {
            stream
              .filterKeySegments(targetSegments.asJava)
              .map[String](e => e.getValue)
              .collect(CacheCollectors.serializableCollector[String, java.util.List[String]](() => Collectors.toList[String]))
          } finally {
            stream.close()
          }

        results
          .asScala
          .foreach { value =>
            println(s"value = ${value}, segment = ${consistentHash.getSegment(value.replace("value", "key"))}, primaryOwner = ${dm.getPrimaryLocation(value.replace("value", "key"))}")
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
