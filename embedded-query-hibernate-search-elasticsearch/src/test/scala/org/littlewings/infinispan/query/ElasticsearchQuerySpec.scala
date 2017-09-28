package org.littlewings.infinispan.query

import org.infinispan.Cache
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.query.Search
import org.scalatest.{FunSuite, Matchers}

class ElasticsearchQuerySpec extends FunSuite with Matchers {
  val indexedBooks: Array[IndexedBook] = Array(
    IndexedBook("978-4798142470", "Spring徹底入門 Spring FrameworkによるJavaアプリケーション開発", 4320, "Spring"),
    IndexedBook("978-4774182179", "［改訂新版］Spring入門 ――Javaフレームワーク・より良い設計とアーキテクチャ", 4104, "Spring"),
    IndexedBook("978-4774161631", "[改訂新版] Apache Solr入門 ~オープンソース全文検索エンジン", 3888, "全文検索"),
    IndexedBook("978-4048662024", "高速スケーラブル検索エンジン ElasticSearch Server", 6915, "全文検索"),
    IndexedBook("978-4774183169", "パーフェクト Java EE", 3456, "Java EE"),
    IndexedBook("978-4798140926", "Java EE 7徹底入門 標準Javaフレームワークによる高信頼性Webシステムの構築", 4104, "Java EE")
  )

  test("simple full-text query") {
    withCache[String, IndexedBook]("indexedBookCache", 3) { cache =>
      indexedBooks.foreach(b => cache.put(b.isbn, b))

      val queryFactory = Search.getQueryFactory(cache)
      val query =
        queryFactory.create(
          """|from org.littlewings.infinispan.query.IndexedBook b
             |where b.price < 5000
             |and b.title: '全文検索'""".stripMargin)

      val resultBooks = query.list[IndexedBook]()
      resultBooks should have size (1)
      resultBooks.get(0).getIsbn should be("978-4774161631")
      resultBooks.get(0).getTitle should be("[改訂新版] Apache Solr入門 ~オープンソース全文検索エンジン")
      resultBooks.get(0).getPrice should be(3888)
      resultBooks.get(0).getCategory should be("全文検索")
    }
  }

  test("parameter-bind and sort") {
    withCache[String, IndexedBook]("indexedBookCache", 3) { cache =>
      indexedBooks.foreach(b => cache.put(b.isbn, b))

      val queryFactory = Search.getQueryFactory(cache)
      val query =
        queryFactory.create(
          """|from org.littlewings.infinispan.query.IndexedBook b
             |where b.price > :price
             |order by b.price desc, b.isbn desc'""".stripMargin)
      query.setParameter("price", 4000)

      val resultBooks = query.list[IndexedBook]()
      resultBooks should have size (4)
      resultBooks.get(0).getIsbn should be("978-4048662024")
      resultBooks.get(0).getTitle should be("高速スケーラブル検索エンジン ElasticSearch Server")
      resultBooks.get(0).getPrice should be(6915)
      resultBooks.get(0).getCategory should be("全文検索")

      resultBooks.get(1).getIsbn should be("978-4798142470")
      resultBooks.get(1).getTitle should be("Spring徹底入門 Spring FrameworkによるJavaアプリケーション開発")
      resultBooks.get(1).getPrice should be(4320)
      resultBooks.get(1).getCategory should be("Spring")

      resultBooks.get(2).getIsbn should be("978-4798140926")
      resultBooks.get(2).getTitle should be("Java EE 7徹底入門 標準Javaフレームワークによる高信頼性Webシステムの構築")
      resultBooks.get(2).getPrice should be(4104)
      resultBooks.get(2).getCategory should be("Java EE")

      resultBooks.get(3).getIsbn should be("978-4774182179")
      resultBooks.get(3).getTitle should be("［改訂新版］Spring入門 ――Javaフレームワーク・より良い設計とアーキテクチャ")
      resultBooks.get(3).getPrice should be(4104)
      resultBooks.get(3).getCategory should be("Spring")
    }
  }

  test("aggregation") {
    withCache[String, IndexedBook]("indexedBookCache", 3) { cache =>
      indexedBooks.foreach(b => cache.put(b.isbn, b))

      val queryFactory = Search.getQueryFactory(cache)
      val query =
        queryFactory.create(
          """|select b.category, sum(b.price)
             |from org.littlewings.infinispan.query.IndexedBook b
             |where b.title: (+'入門' and -'検索')
             |group by b.category
             |order by sum(b.price) desc""".stripMargin)

      val results = query.list[Array[AnyRef]]()
      results should have size (2)
      results.get(0)(0) should be("Spring")
      results.get(0)(1) should be(8424)
      results.get(1)(0) should be("Java EE")
      results.get(1)(1) should be(4104)
    }
  }

  test("re-indexing") {
    withCache[String, IndexedBook]("indexedBookCache", 3) { cache =>
      indexedBooks.foreach(b => cache.put(b.isbn, b))

      val queryFactory = Search.getQueryFactory(cache)
      val query1 =
        queryFactory.create(
          """|from org.littlewings.infinispan.query.IndexedBook b
             |where b.price < 5000
             |and b.title: '全文検索'""".stripMargin)

      val resultBooks1 = query1.list[IndexedBook]()
      resultBooks1 should have size (1)
      resultBooks1.get(0).getIsbn should be("978-4774161631")

      val massIndexer = Search.getSearchManager(cache).getMassIndexer
      massIndexer.start()

      val query2 =
        queryFactory.create(
          """|from org.littlewings.infinispan.query.IndexedBook b
             |where b.price < 5000
             |and b.title: '全文検索'""".stripMargin)

      val resultBooks2 = query2.list[IndexedBook]()
      resultBooks2 should have size (1)
      resultBooks2.get(0).getIsbn should be("978-4774161631")
    }
  }

  test("Hibernate Search, native query") {
    withCache[String, IndexedBook]("indexedBookCache", 3) { cache =>
      indexedBooks.foreach(b => cache.put(b.isbn, b))

      val searchManager = Search.getSearchManager(cache)
      val queryBuilder = searchManager.buildQueryBuilderForClass(classOf[IndexedBook]).get
      val query =
        queryBuilder
          .bool
          .must(queryBuilder.keyword.onField("title").matching("全件検索").createQuery)
          .must(queryBuilder.range.onField("price").below(5000).createQuery)
          .createQuery
      val cacheQuery = searchManager.getQuery[IndexedBook](query, classOf[IndexedBook])

      val resultBooks = cacheQuery.list()
      resultBooks should have size (1)
      resultBooks.get(0).getIsbn should be("978-4774161631")
      resultBooks.get(0).getTitle should be("[改訂新版] Apache Solr入門 ~オープンソース全文検索エンジン")
      resultBooks.get(0).getPrice should be(3888)
      resultBooks.get(0).getCategory should be("全文検索")
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
