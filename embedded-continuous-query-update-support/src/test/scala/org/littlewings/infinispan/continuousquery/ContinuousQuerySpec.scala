package org.littlewings.infinispan.continuousquery

import org.infinispan.Cache
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.objectfilter.ParsingException
import org.infinispan.query.Search
import org.scalatest.{FunSuite, Matchers}

class ContinuousQuerySpec extends FunSuite with Matchers {
  test("continuous query, simple") {
    withCache[String, Book]("bookCache", 3) { cache =>
      val continuousQuery = Search.getContinuousQuery(cache)
      val continuousQueryListener = new MyContinuousQueryListener

      val query =
        Search
          .getQueryFactory(cache)
          .create(
            s"""|from ${classOf[Book].getName} b
                |where b.title like '%Infinispan%'
                |and b.price >= :priceLower
                |and b.price <= :priceUpper""".stripMargin)
      query.setParameter("priceLower", 3000)
      query.setParameter("priceUpper", 5000)

      continuousQuery.addContinuousQueryListener(query, continuousQueryListener)

      val book = Book("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 2000)

      // none
      cache.put(book.isbn, book)
      continuousQueryListener.allCount should be((0, 0, 0))

      // join1
      val book3000 = book.newBookChangePrice(3000)
      cache.put(book3000.isbn, book3000)
      continuousQueryListener.allCount should be((1, 0, 0))

      // update
      val book4500 = book3000.newBookChangePrice(4500)
      cache.put(book4500.isbn, book4500)
      continuousQueryListener.allCount should be((1, 1, 0))

      // leave1
      val book5001 = book4500.newBookChangePrice(5001)
      cache.put(book5001.isbn, book5001)
      continuousQueryListener.allCount should be((1, 1, 1))

      cache.remove(book.isbn)
      continuousQueryListener.allCount should be((1, 1, 1))

      // join2
      cache.put(book3000.isbn, book3000)
      continuousQueryListener.allCount should be((2, 1, 1))

      // leave2
      cache.remove(book3000.isbn)
      continuousQueryListener.allCount should be((2, 1, 2))

      val ignoredBook = Book("978-1785285332", "Getting Started With Hazelcast", 3904)
      cache.put(ignoredBook.isbn, ignoredBook)
      continuousQueryListener.allCount should be((2, 1, 2))
    }
  }

  test("continuous query, projection") {
    withCache[String, Book]("bookCache", 3) { cache =>
      val continuousQuery = Search.getContinuousQuery(cache)
      val continuousQueryListener = new MyProjectionSupportContinuousQueryListener

      val queryParameters = new java.util.HashMap[String, AnyRef]
      queryParameters.put("priceLower", Integer.valueOf(3000))
      queryParameters.put("priceUpper", Integer.valueOf(5000))

      continuousQuery
        .addContinuousQueryListener(
          s"""|select b.title, b.price
              |from ${classOf[Book].getName} b
              |where b.price >= :priceLower
              |and b.price <= :priceUpper""".stripMargin,
          queryParameters,
          continuousQueryListener)

      val book = Book("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 2000)

      // none
      cache.put(book.isbn, book)
      continuousQueryListener.allCount should be((0, 0, 0))

      // join
      val book3000 = book.newBookChangePrice(3000)
      cache.put(book3000.isbn, book3000)
      continuousQueryListener.allCount should be((1, 0, 0))

      // remove
      cache.remove(book3000.isbn)
      continuousQueryListener.allCount should be((1, 0, 1))
    }
  }

  test("continuous query, unsupported full-text query") {
    withCache[String, IndexedBook]("indexedBookCache", 3) { cache =>
      val continuousQuery = Search.getContinuousQuery(cache)
      val continuousQueryListener = new MyIndexedContinuousQueryListener

      val query =
        Search
          .getQueryFactory(cache)
          .create(
            s"""|from ${classOf[IndexedBook].getName} b
                |where b.title: 'infinispan'""".stripMargin)

      val thrown = the[ParsingException] thrownBy continuousQuery.addContinuousQueryListener(query, continuousQueryListener)
      thrown.getMessage should be("ISPN028523: Filters cannot use full-text searches")
    }
  }

  test("continuous query, indexed") {
    withCache[String, IndexedBook]("indexedBookCache", 3) { cache =>
      val continuousQuery = Search.getContinuousQuery(cache)
      val continuousQueryListener = new MyIndexedContinuousQueryListener

      val query =
        Search
          .getQueryFactory(cache)
          .create(
            s"""|from ${classOf[IndexedBook].getName} b
                |where b.price >= :priceLower
                |and b.price <= :priceUpper""".stripMargin)
      query.setParameter("priceLower", 3000)
      query.setParameter("priceUpper", 5000)

      continuousQuery.addContinuousQueryListener(query, continuousQueryListener)

      val book = IndexedBook("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 2000)

      // none
      cache.put(book.isbn, book)
      continuousQueryListener.allCount should be((0, 0, 0))

      // join1
      val book3000 = book.newBookChangePrice(3000)
      cache.put(book3000.isbn, book3000)
      continuousQueryListener.allCount should be((1, 0, 0))

      // update
      val book4500 = book3000.newBookChangePrice(4500)
      cache.put(book4500.isbn, book4500)
      continuousQueryListener.allCount should be((1, 1, 0))

      // leave1
      val book5001 = book4500.newBookChangePrice(5001)
      cache.put(book5001.isbn, book5001)
      continuousQueryListener.allCount should be((1, 1, 1))

      cache.remove(book.isbn)
      continuousQueryListener.allCount should be((1, 1, 1))

      // join2
      cache.put(book3000.isbn, book3000)
      continuousQueryListener.allCount should be((2, 1, 1))

      // leave2
      cache.remove(book3000.isbn)
      continuousQueryListener.allCount should be((2, 1, 2))
    }
  }

  test("continuous query, indexed, projection") {
    withCache[String, IndexedBook]("indexedBookCache", 3) { cache =>
      val continuousQuery = Search.getContinuousQuery(cache)
      val continuousQueryListener = new MyProjectionSupportContinuousQueryListener

      val queryParameters = new java.util.HashMap[String, AnyRef]
      queryParameters.put("priceLower", Integer.valueOf(3000))
      queryParameters.put("priceUpper", Integer.valueOf(5000))

      continuousQuery
        .addContinuousQueryListener(
          s"""|select b.title, b.price
              |from ${classOf[IndexedBook].getName} b
              |where b.price >= :priceLower
              |and b.price <= :priceUpper""".stripMargin,
          queryParameters,
          continuousQueryListener)

      val book = IndexedBook("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 2000)

      // none
      cache.put(book.isbn, book)
      continuousQueryListener.allCount should be((0, 0, 0))

      val book3000 = book.newBookChangePrice(3000)
      cache.put(book3000.isbn, book3000)
      continuousQueryListener.allCount should be((1, 0, 0))
    }
  }

  def withCache[K, V](cacheName: String, numInstances: Int = 1)(fun: Cache[K, V] => Unit): Unit = {
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
