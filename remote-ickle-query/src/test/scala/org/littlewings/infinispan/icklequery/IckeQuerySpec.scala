package org.littlewings.infinispan.icklequery

import org.infinispan.client.hotrod.configuration.ConfigurationBuilder
import org.infinispan.client.hotrod.exceptions.HotRodClientException
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller
import org.infinispan.client.hotrod.{RemoteCache, RemoteCacheManager, Search}
import org.infinispan.protostream.annotations.ProtoSchemaBuilder
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants
import org.scalatest.{FunSuite, Matchers}

class IckeQuerySpec extends FunSuite with Matchers {
  val books: Array[Book] = Array(
    Book("978-4798142470", "Spring徹底入門 Spring FrameworkによるJavaアプリケーション開発", 4320, "Spring"),
    Book("978-4774182179", "［改訂新版］Spring入門 ――Javaフレームワーク・より良い設計とアーキテクチャ", 4104, "Spring"),
    Book("978-4774161631", "[改訂新版] Apache Solr入門 ~オープンソース全文検索エンジン", 3888, "全文検索"),
    Book("978-4048662024", "高速スケーラブル検索エンジン ElasticSearch Server", 6915, "全文検索"),
    Book("978-4774183169", "パーフェクト Java EE", 3456, "Java EE"),
    Book("978-4798140926", "Java EE 7徹底入門 標準Javaフレームワークによる高信頼性Webシステムの構築", 4104, "Java EE")
  )

  test("index-less simple Ickle Query") {
    withCache[String, Book]("bookCache") { cache =>
      val manager = cache.getRemoteCacheManager

      val context = ProtoStreamMarshaller.getSerializationContext(manager)
      val protoSchemaBuilder = new ProtoSchemaBuilder
      val idl =
        protoSchemaBuilder
          .fileName("book.proto")
          .addClass(classOf[Book])
          .build(context)

      val metaCache =
        manager.getCache[String, AnyRef](ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME)
      metaCache.put("book.proto", idl)

      books.foreach(b => cache.put(b.isbn, b))

      val queryFactory = Search.getQueryFactory(cache)
      val query =
        queryFactory.create(
          """|from Book b
             |where b.price > 5000
             |and b.title = '高速スケーラブル検索エンジン ElasticSearch Server'""".stripMargin)

      val resultBooks = query.list[Book]()
      resultBooks should have size (1)
      resultBooks.get(0).isbn should be("978-4048662024")
      resultBooks.get(0).title should be("高速スケーラブル検索エンジン ElasticSearch Server")
      resultBooks.get(0).price should be(6915)
      resultBooks.get(0).category should be("全文検索")
    }
  }

  test("index-less simple Ickle Query, parameterized") {
    withCache[String, Book]("bookCache") { cache =>
      val manager = cache.getRemoteCacheManager

      val context = ProtoStreamMarshaller.getSerializationContext(manager)
      val protoSchemaBuilder = new ProtoSchemaBuilder
      val idl =
        protoSchemaBuilder
          .fileName("book.proto")
          .addClass(classOf[Book])
          .build(context)

      val metaCache =
        manager.getCache[String, AnyRef](ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME)
      metaCache.put("book.proto", idl)

      books.foreach(b => cache.put(b.isbn, b))

      val queryFactory = Search.getQueryFactory(cache)
      val query =
        queryFactory.create(
          """|from Book b
             |where b.price > :price
             |and b.title = :title""".stripMargin)
      query.setParameter("price", 5000)
      query.setParameter("title", "高速スケーラブル検索エンジン ElasticSearch Server")

      val resultBooks = query.list[Book]()
      resultBooks should have size (1)
      resultBooks.get(0).getIsbn should be("978-4048662024")
      resultBooks.get(0).getTitle should be("高速スケーラブル検索エンジン ElasticSearch Server")
      resultBooks.get(0).getPrice should be(6915)
      resultBooks.get(0).getCategory should be("全文検索")
    }
  }

  test("index-less simple Ickle Query, aggregation") {
    withCache[String, Book]("bookCache") { cache =>
      val manager = cache.getRemoteCacheManager

      val context = ProtoStreamMarshaller.getSerializationContext(manager)
      val protoSchemaBuilder = new ProtoSchemaBuilder
      val idl =
        protoSchemaBuilder
          .fileName("book.proto")
          .addClass(classOf[Book])
          .build(context)

      val metaCache =
        manager.getCache[String, AnyRef](ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME)
      metaCache.put("book.proto", idl)

      books.foreach(b => cache.put(b.isbn, b))

      val queryFactory = Search.getQueryFactory(cache)
      val query =
        queryFactory.create(
          """|select b.category, sum(b.price)
             |from Book b
             |where b.price > :price
             |group by b.category
             |having sum(b.price) > :sumPrice
             |order by sum(b.price) desc""".stripMargin)

      query.setParameter("price", 4000)
      query.setParameter("sumPrice", 5000)

      val results = query.list[Array[AnyRef]]()
      results should have size (2)
      results.get(0)(0) should be("Spring")
      results.get(0)(1) should be(8424)
      results.get(1)(0) should be("全文検索")
      results.get(1)(1) should be(6915)
    }
  }

  val indexedBooks: Array[IndexedBook] = Array(
    IndexedBook("978-4798142470", "Spring徹底入門 Spring FrameworkによるJavaアプリケーション開発", 4320, "Spring"),
    IndexedBook("978-4774182179", "［改訂新版］Spring入門 ――Javaフレームワーク・より良い設計とアーキテクチャ", 4104, "Spring"),
    IndexedBook("978-4774161631", "[改訂新版] Apache Solr入門 ~オープンソース全文検索エンジン", 3888, "全文検索"),
    IndexedBook("978-4048662024", "高速スケーラブル検索エンジン ElasticSearch Server", 6915, "全文検索"),
    IndexedBook("978-4774183169", "パーフェクト Java EE", 3456, "Java EE"),
    IndexedBook("978-4798140926", "Java EE 7徹底入門 標準Javaフレームワークによる高信頼性Webシステムの構築", 4104, "Java EE")
  )

  test("indexed entity Ickle Query, full text query") {
    withCache[String, IndexedBook]("indexedBookCache") { cache =>
      val manager = cache.getRemoteCacheManager

      val context = ProtoStreamMarshaller.getSerializationContext(manager)
      val protoSchemaBuilder = new ProtoSchemaBuilder
      val idl =
        protoSchemaBuilder
          .fileName("indexed-book.proto")
          .addClass(classOf[IndexedBook])
          .build(context)

      val metaCache =
        manager.getCache[String, AnyRef](ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME)
      metaCache.put("indexed-book.proto", idl)

      indexedBooks.foreach(b => cache.put(b.isbn, b))

      val queryFactory = Search.getQueryFactory(cache)
      val query =
        queryFactory.create(
          """|from IndexedBook b
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

  test("indexed entity Ickle Query, full text query with range") {
    withCache[String, IndexedBook]("indexedBookCache") { cache =>
      val manager = cache.getRemoteCacheManager

      val context = ProtoStreamMarshaller.getSerializationContext(manager)
      val protoSchemaBuilder = new ProtoSchemaBuilder
      val idl =
        protoSchemaBuilder
          .fileName("indexed-book.proto")
          .addClass(classOf[IndexedBook])
          .build(context)

      val metaCache =
        manager.getCache[String, AnyRef](ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME)
      metaCache.put("indexed-book.proto", idl)

      indexedBooks.foreach(b => cache.put(b.isbn, b))

      val queryFactory = Search.getQueryFactory(cache)
      val query =
        queryFactory.create(
          """|from IndexedBook b
             |where b.price: [6000 TO 7000]
             |and b.title: '検索'""".stripMargin)

      val resultBooks = query.list[IndexedBook]()
      resultBooks should have size (1)
      resultBooks.get(0).getIsbn should be("978-4048662024")
      resultBooks.get(0).getTitle should be("高速スケーラブル検索エンジン ElasticSearch Server")
      resultBooks.get(0).getPrice should be(6915)
      resultBooks.get(0).getCategory should be("全文検索")
    }
  }

  test("index-less Ickle Query, can't applied full text predicate") {
    withCache[String, Book]("bookCache") { cache =>
      val manager = cache.getRemoteCacheManager

      val context = ProtoStreamMarshaller.getSerializationContext(manager)
      val protoSchemaBuilder = new ProtoSchemaBuilder
      val idl =
        protoSchemaBuilder
          .fileName("book.proto")
          .addClass(classOf[Book])
          .build(context)

      val metaCache =
        manager.getCache[String, AnyRef](ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME)
      metaCache.put("book.proto", idl)

      books.foreach(b => cache.put(b.isbn, b))

      val queryFactory = Search.getQueryFactory(cache)
      val query =
        queryFactory.create(
          """|from Book b
             |where b.title: '高速スケーラブル検索エンジン ElasticSearch Server'""".stripMargin)

      val thrown =
        the[HotRodClientException] thrownBy query.list[Book]()

      thrown.getMessage should be("org.infinispan.objectfilter.ParsingException: ISPN028521: Full-text queries cannot be applied to property 'title' in type Book unless the property is indexed and analyzed.")
    }
  }

  test("indexed entity Ickle Query, analyzed field can't applied eq") {
    withCache[String, IndexedBook]("indexedBookCache") { cache =>
      val manager = cache.getRemoteCacheManager

      val context = ProtoStreamMarshaller.getSerializationContext(manager)
      val protoSchemaBuilder = new ProtoSchemaBuilder
      val idl =
        protoSchemaBuilder
          .fileName("indexed-book.proto")
          .addClass(classOf[IndexedBook])
          .build(context)

      val metaCache =
        manager.getCache[String, AnyRef](ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME)
      metaCache.put("indexed-book.proto", idl)

      indexedBooks.foreach(b => cache.put(b.isbn, b))

      val queryFactory = Search.getQueryFactory(cache)
      val query =
        queryFactory.create(
            """|from IndexedBook b
               |where b.price > 5000
               |and b.title = '全文検索'""".stripMargin)

      val thrown =
        the[HotRodClientException] thrownBy query.list[IndexedBook]()
      thrown.getMessage should be("org.infinispan.objectfilter.ParsingException: ISPN028522: No relational queries can be applied to property 'title' in type IndexedBook since the property is analyzed.")
    }
  }

  test("indexed entity Ickle Query, full text query, aggregation") {
    withCache[String, IndexedBook]("indexedBookCache") { cache =>
      val manager = cache.getRemoteCacheManager

      val context = ProtoStreamMarshaller.getSerializationContext(manager)
      val protoSchemaBuilder = new ProtoSchemaBuilder
      val idl =
        protoSchemaBuilder
          .fileName("indexed-book.proto")
          .addClass(classOf[IndexedBook])
          .build(context)

      val metaCache =
        manager.getCache[String, AnyRef](ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME)
      metaCache.put("indexed-book.proto", idl)

      indexedBooks.foreach(b => cache.put(b.isbn, b))

      val queryFactory = Search.getQueryFactory(cache)
      val query =
        queryFactory.create(
          """|select b.category, sum(b.price)
             |from IndexedBook b
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

  protected def withCache[K, V](cacheName: String)(fun: RemoteCache[K, V] => Unit): Unit = {
    val manager =
      new RemoteCacheManager(
        new ConfigurationBuilder()
          .addServer()
          .host("localhost")
          .port(11222)
          .marshaller(new ProtoStreamMarshaller)
          .build()
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
