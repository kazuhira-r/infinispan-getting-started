package org.littlewings.infinispan.distiterator

import java.util.Map

import org.infinispan.Cache
import org.infinispan.commons.util.CloseableIterator
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.util.function.{SerializableConsumer, SerializableFunction}
import org.scalatest.{FunSuite, Matchers}

import scala.collection.JavaConverters._

class DistributedIteratorSuite extends FunSuite with Matchers {
  test("distributed cache iterator") {
    withCache[String, Integer]("distributedCache", 3) { cache =>
      (1 to 10).foreach(i => cache.put(s"key${i}", i))

      val iterator: CloseableIterator[java.util.Map.Entry[String, Integer]] =
        cache.entrySet.iterator.asInstanceOf[CloseableIterator[java.util.Map.Entry[String, Integer]]]

      /*
      while (iterator.hasNext()) {
        println(iterator.next())
      }
      */

      iterator.asScala.toList.map(e => e.getKey -> e.getValue) should contain theSameElementsAs Array(
        "key1" -> 1, "key2" -> 2, "key3" -> 3, "key4" -> 4, "key5" -> 5,
        "key6" -> 6, "key7" -> 7, "key8" -> 8, "key9" -> 9, "key10" -> 10
      )

      iterator.close()
    }
  }

  test("replicated cache iterator") {
    withCache[String, Integer]("replicatedCache", 3) { cache =>
      (1 to 10).foreach(i => cache.put(s"key${i}", i))

      val iterator: CloseableIterator[java.util.Map.Entry[String, Integer]] =
        cache.entrySet.iterator.asInstanceOf[CloseableIterator[java.util.Map.Entry[String, Integer]]]

      /*
      while (iterator.hasNext()) {
        println(iterator.next())
      }
      */

      iterator.asScala.toList.map(e => e.getKey -> e.getValue) should contain theSameElementsAs Array(
        "key1" -> 1, "key2" -> 2, "key3" -> 3, "key4" -> 4, "key5" -> 5,
        "key6" -> 6, "key7" -> 7, "key8" -> 8, "key9" -> 9, "key10" -> 10
      )

      iterator.close()
    }
  }

  test("local cache iterator") {
    withCache[String, Integer]("localCache") { cache =>
      (1 to 10).foreach(i => cache.put(s"key${i}", i))

      val iterator: CloseableIterator[java.util.Map.Entry[String, Integer]] =
        cache.entrySet.iterator.asInstanceOf[CloseableIterator[java.util.Map.Entry[String, Integer]]]

      /*
      while (iterator.hasNext()) {
        println(iterator.next())
      }
      */

      iterator.asScala.toList.map(e => e.getKey -> e.getValue) should contain theSameElementsAs Array(
        "key1" -> 1, "key2" -> 2, "key3" -> 3, "key4" -> 4, "key5" -> 5,
        "key6" -> 6, "key7" -> 7, "key8" -> 8, "key9" -> 9, "key10" -> 10
      )

      iterator.close()
    }
  }

  test("distributed cache stream") {
    withCache[String, Integer]("distributedCache", 3) { cache =>
      (1 to 10).foreach(i => cache.put(s"key${i}", i))

      val stream = cache.entrySet.stream
      try {
        stream
          .map[String](new SerializableFunction[java.util.Map.Entry[String, Integer], String] {
          override def apply(e: Map.Entry[String, Integer]): String = e.getKey
        })
          .forEach(new SerializableConsumer[String] {
            override def accept(v: String): Unit = println(v)
          })
      } finally {
        stream.close()
      }
    }
  }

  protected def withCache[K, V](cacheName: String, numInstances: Int = 1)(fun: Cache[K, V] => Unit): Unit = {
    val managers = (1 to numInstances).map(_ => new DefaultCacheManager("infinispan.xml"))

    try {
      managers.foreach(_.getCache[K, V](cacheName))

      val cache = managers(0).getCache[K, V](cacheName)
      fun(cache)
      cache.stop()
    } finally {
      managers.foreach(_.stop())
    }
  }
}
