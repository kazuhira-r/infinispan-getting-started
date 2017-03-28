package org.littlewings.infinispan.task

import org.infinispan.client.hotrod.configuration.ConfigurationBuilder
import org.infinispan.client.hotrod.{RemoteCache, RemoteCacheManager}
import org.littlewings.infinispan.task.entity.Book
import org.scalatest.{FunSuite, Matchers}

import scala.collection.JavaConverters._

class ServerTaskSpec extends FunSuite with Matchers {
  test("one-node simple-task") {
    withRemoteCache[String, String]("simpleCache") { cache =>
      val parameters = Map("name" -> "MyFirstTask").asJava
      val result = cache.execute[String]("oneNodeSimpleTask", parameters)
      // result should be("Hello MyFirstTask!!, Cache[simpleCache] by infinispan1")
      result should (be("Hello MyFirstTask!!, Cache[default] by infinispan1")
        or be("Hello MyFirstTask!!, Cache[simpleCache] by infinispan2")
        or be("Hello MyFirstTask!!, Cache[simpleCache] by infinispan3")
        )
      println(result)
    }
  }

  test("all-node simple-task") {
    withRemoteCache[String, String]("simpleCache") { cache =>
      val parameters = Map("name" -> "MyFirstTask").asJava
      val results = cache.execute[java.util.List[String]]("allNodeSimpleTask", parameters)
      results should (contain("Hello MyFirstTask!!, Cache[simpleCache] by infinispan1")
        and contain("Hello MyFirstTask!!, Cache[simpleCache] by infinispan2")
        and contain("Hello MyFirstTask!!, Cache[simpleCache] by infinispan3"))
    }
  }

  test("one-node cache-task") {
    withRemoteCache[String, String]("simpleCache") { cache =>
      val parameters = Map("key" -> "simpleKey").asJava
      cache.put("simpleKey", "simpleValue")

      val result = cache.execute[String]("cacheSimpleTask", parameters)
      result should be("key = simpleKey, value = simpleValue")
    }
  }

  test("one-node include original-class") {
    withRemoteCache[String, Book]("bookCache") { cache =>
      val book = new Book("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 4543)

      cache.put(book.getIsbn, book)

      val parameters = Map("target" -> book).asJava

      val resultBook = cache.execute[Book]("bookPriceDoublingTask", parameters)

      resultBook.getIsbn should be(book.getIsbn)
      resultBook.getTitle should be(book.getTitle)
      resultBook.getPrice should be(book.getPrice * 2)
    }
  }

  test("one-node include original-class, with Compatibility mode") {
    withRemoteCache[String, Book]("compatibilityBookCache") { cache =>
      val book = new Book("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 4543)

      cache.put(book.getIsbn, book)

      val parameters = Map("target" -> book).asJava

      val resultBook = cache.execute[Book]("compatibilityBookPriceDoublingTask", parameters)

      resultBook.getIsbn should be(book.getIsbn)
      resultBook.getTitle should be(book.getTitle)
      resultBook.getPrice should be(book.getPrice * 2)
    }
  }

  test("one-node include original-class using Distributed Stream API") {
    withRemoteCache[String, Book]("compatibilityBookCache") { cache =>
      val books = Array(
        new Book("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 4543),
        new Book("978-1785285332", "Getting Started With Hazelcast", 3533),
        new Book("978-1849519205", "Hibernate Search by Example", 3365),
        new Book("978-1783988181", "Mastering Redis", 6102),
        new Book("978-1491933664", "Cassandra: The Definitive Guide", 4637),
        new Book("978-1449344689", "MongoDB: The Definitive Guide", 3925),
        new Book("978-1449358549", "Elasticsearch: The Definitive Guide", 5951),
        new Book("978-1784399641", "Apache Solr Essentials", 3301),
        new Book("978-1449396107", "HBase: The Definitive Guide", 4680)
      )

      books.foreach(b => cache.put(b.getIsbn, b))

      val result = cache.execute[Integer]("bookPriceSumTask", new java.util.HashMap[String, AnyRef])
      result should be(40037)
    }
  }

  def withRemoteCache[K, V](cacheName: String)(fun: RemoteCache[K, V] => Unit): Unit = {
    val manager = new RemoteCacheManager(
      new ConfigurationBuilder().addServers("172.17.0.2:11222").build()
    )

    try {
      val cache = manager.getCache[K, V](cacheName)
      fun(cache)
      cache.stop()
    } finally {
      manager.stop()
    }
  }
}
