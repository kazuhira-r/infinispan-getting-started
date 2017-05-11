package org.littlewings.infinispan.datacontainers

import org.infinispan.Cache
import org.infinispan.commons.marshall.WrappedByteArray
import org.infinispan.configuration.cache.StorageType
import org.infinispan.container.DefaultDataContainer
import org.infinispan.container.entries.{CacheEntrySizeCalculator, ImmortalCacheEntry, InternalCacheEntry, PrimitiveEntrySizeCalculator}
import org.infinispan.container.offheap.{BoundedOffHeapDataContainer, OffHeapDataContainer}
import org.infinispan.eviction.EvictionType
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.marshall.core.WrappedByteArraySizeCalculator
import org.scalatest.{FunSuite, Matchers}

class DataContainersSpec extends FunSuite with Matchers {
  val books: Array[Book] = Array(
    Book("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 5577),
    Book("978-1785285332", "Getting Started With Hazelcast", 4338),
    Book("978-1365732355", "High Performance In-Memory Computing with Apache Ignite", 4815),
    Book("978-1849519205", "Hibernate Search by Example", 3718),
    Book("978-1784392413", "Wildfly Cookbook", 6817),
    Book("978-1783987146", "Mastering Apache Spark", 6615),
    Book("978-1491974292", "Stream Processing With Apache Flink", 5222),
    Book("978-1449358549", "Elasticsearch: The Definitive Guide", 6072),
    Book("978-1617291029", "Solr in Action", 5772),
    Book("978-1782162285", "Lucene 4 Cookbook", 5577)
  )

  test("default container") {
    withCache[String, Book]("defaultCache", 3) { cache =>
      val memoryConfiguration = cache.getCacheConfiguration.memory
      memoryConfiguration.size should be(-1)
      memoryConfiguration.storageType should be(StorageType.OBJECT)
      memoryConfiguration.addressCount should be(1048576)
      memoryConfiguration.evictionType should be(EvictionType.COUNT)

      cache.getAdvancedCache.getDataContainer should be(a[DefaultDataContainer[_, _]])

      books.foreach(b => cache.put(b.isbn, b))
      cache should have size (10)
    }
  }

  test("default container, object memory, with size") {
    withCache[String, Book]("objectWithSizeCache", 3) { cache =>
      val memoryConfiguration = cache.getCacheConfiguration.memory
      memoryConfiguration.size should be(3)
      memoryConfiguration.storageType should be(StorageType.OBJECT)
      memoryConfiguration.addressCount should be(1048576)
      memoryConfiguration.evictionType should be(EvictionType.COUNT)

      cache.getAdvancedCache.getDataContainer should be(a[DefaultDataContainer[_, _]])

      books.foreach(b => cache.put(b.isbn, b))
      cache.size should (be >= 3 and be <= 6)
    }
  }

  test("default container, binary memory") {
    withCache[String, Book]("binaryCache", 3) { cache =>
      val memoryConfiguration = cache.getCacheConfiguration.memory
      memoryConfiguration.size should be(-1)
      memoryConfiguration.storageType should be(StorageType.BINARY)
      memoryConfiguration.addressCount should be(1048576)
      memoryConfiguration.evictionType should be(EvictionType.COUNT)

      cache.getAdvancedCache.getDataContainer should be(a[DefaultDataContainer[_, _]])

      books.foreach(b => cache.put(b.isbn, b))
      cache should have size 10
    }
  }

  test("default container, binary memory, by count") {
    withCache[String, Book]("binaryByCountCache", 3) { cache =>
      val memoryConfiguration = cache.getCacheConfiguration.memory
      memoryConfiguration.size should be(3)
      memoryConfiguration.storageType should be(StorageType.BINARY)
      memoryConfiguration.addressCount should be(1048576)
      memoryConfiguration.evictionType should be(EvictionType.COUNT)

      cache.getAdvancedCache.getDataContainer should be(a[DefaultDataContainer[_, _]])

      books.foreach(b => cache.put(b.isbn, b))
      cache.size should (be >= 4 and be <= 6)
    }
  }

  test("default container, binary memory, by memory") {
    withCache[String, Book]("binaryByMemoryCache", 3) { cache =>
      val memoryConfiguration = cache.getCacheConfiguration.memory
      memoryConfiguration.size should be(1000)
      memoryConfiguration.storageType should be(StorageType.BINARY)
      memoryConfiguration.addressCount should be(1048576)
      memoryConfiguration.evictionType should be(EvictionType.MEMORY)

      cache.getAdvancedCache.getDataContainer should be(a[DefaultDataContainer[_, _]])

      val marshaller = cache.getAdvancedCache.getComponentRegistry.getCacheMarshaller
      val calcurator = new CacheEntrySizeCalculator[String, WrappedByteArray](new WrappedByteArraySizeCalculator(new PrimitiveEntrySizeCalculator))
      val entriesSize =
        books
          .map(b => calcurator
            .calculateSize(b.isbn, new ImmortalCacheEntry(b.isbn, new WrappedByteArray(marshaller.objectToByteBuffer(b))).asInstanceOf[InternalCacheEntry[String, WrappedByteArray]]))

      entriesSize.min should be(264)
      entriesSize.max should be(304)

      books.foreach(b => cache.put(b.isbn, b))
      cache.size should (be >= 2 and be <= 6)
    }
  }

  test("off-heap container") {
    withCache[String, Book]("offHeapCache", 3) { cache =>
      val memoryConfiguration = cache.getCacheConfiguration.memory
      memoryConfiguration.size should be(-1)
      memoryConfiguration.storageType should be(StorageType.OFF_HEAP)
      memoryConfiguration.addressCount should be(1048576)
      memoryConfiguration.evictionType should be(EvictionType.COUNT)

      cache.getAdvancedCache.getDataContainer should be(a[OffHeapDataContainer])
      cache.getAdvancedCache.getDataContainer should not be a[BoundedOffHeapDataContainer]

      books.foreach(b => cache.put(b.isbn, b))
      cache should have size (10)
    }
  }

  test("off-heap container, by count") {
    withCache[String, Book]("offHeapByCountCache", 3) { cache =>
      val memoryConfiguration = cache.getCacheConfiguration.memory
      memoryConfiguration.size should be(3)
      memoryConfiguration.storageType should be(StorageType.OFF_HEAP)
      memoryConfiguration.addressCount should be(1048576)
      memoryConfiguration.evictionType should be(EvictionType.COUNT)

      cache.getAdvancedCache.getDataContainer should be(a[BoundedOffHeapDataContainer])

      books.foreach(b => cache.put(b.isbn, b))
      cache.size should (be >= 3 and be <= 6)
    }
  }

  test("off-heap container, by memory") {
    withCache[String, Book]("offHeapByMemoryCache", 3) { cache =>
      val memoryConfiguration = cache.getCacheConfiguration.memory
      memoryConfiguration.size should be(1000)
      memoryConfiguration.storageType should be(StorageType.OFF_HEAP)
      memoryConfiguration.addressCount should be(1048576)
      memoryConfiguration.evictionType should be(EvictionType.MEMORY)

      cache.getAdvancedCache.getDataContainer should be(a[BoundedOffHeapDataContainer])

      books.foreach(b => cache.put(b.isbn, b))
      cache.size should (be >= 3 and be <= 7)
    }
  }

  test("address to memory size") {
    def calcMemorySize(desiredSize: Int): Long = {
      val maxLockCount = 1 << 30

      def nextPowerOfTwo(target: Int): Int = {
        var n = target - 1
        n |= n >>> 1
        n |= n >>> 2
        n |= n >>> 4
        n |= n >>> 8
        n |= n >>> 16

        if (n < 0) {
          1
        } else if (n >= maxLockCount) {
          maxLockCount
        } else {
          n + 1
        }
      }

      val lockCount = nextPowerOfTwo(Runtime.getRuntime.availableProcessors) << 1
      var memoryAddresses = if (desiredSize >= maxLockCount) maxLockCount else lockCount

      while (memoryAddresses < desiredSize) {
        memoryAddresses <<= 1
      }

      val pointerCount = nextPowerOfTwo(memoryAddresses)
      println(pointerCount)
      pointerCount.toLong << 3
    }

    calcMemorySize(1048576) should be(8388608L)
    (calcMemorySize(1048576) / 1024 / 1024) should be(8)

    calcMemorySize(1048588) should be(16777216L)
    (calcMemorySize(1048588) / 1024 / 1024) should be(16)
  }

  test("off-heap container, by count, with address-count") {
    withCache[String, Book]("offHeapByCountWithAddressCountCache", 3) { cache =>
      val memoryConfiguration = cache.getCacheConfiguration.memory
      memoryConfiguration.size should be(3)
      memoryConfiguration.storageType should be(StorageType.OFF_HEAP)
      memoryConfiguration.addressCount should be(2097152)
      memoryConfiguration.evictionType should be(EvictionType.COUNT)

      cache.getAdvancedCache.getDataContainer should be(a[BoundedOffHeapDataContainer])

      books.foreach(b => cache.put(b.isbn, b))
      cache.size should (be >= 3 and be <= 6)
    }
  }

  protected def withCache[K, V](cacheName: String, numInstances: Int = 1)(fun: Cache[K, V] => Unit): Unit = {
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
