package org.littlewings.infinispan.remotecq

import java.util.concurrent.TimeUnit

import org.infinispan.client.hotrod.configuration.ConfigurationBuilder
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller
import org.infinispan.client.hotrod.{RemoteCache, RemoteCacheManager, Search}
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.protostream.annotations.ProtoSchemaBuilder
import org.infinispan.query.dsl.Query
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants
import org.infinispan.server.hotrod.HotRodServer
import org.infinispan.server.hotrod.configuration.HotRodServerConfigurationBuilder
import org.scalatest.{FunSpec, Matchers}

class RemoteContinuousQuerySpec extends FunSpec with Matchers {
  describe("Remote Continuous Query Spec") {
    it("add Entry") {
      withRemoteCacheServer {
        withRemoteCache[String, Book]("namedCache") { cache =>
          registerProtocolBufIdl(cache, classOf[Book])

          val books = Array(
            Book("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 4947),
            Book("978-1785285332", "Getting Started With Hazelcast - Second Edition", 3848),
            Book("978-1783988181", "Mastering Redis", 6172)
          )

          val continousQuery = Search.getContinuousQuery(cache)

          val query =
            Search
              .getQueryFactory(cache)
              .from(classOf[Book])
              .having("price")
              .gte(4000)
              .toBuilder[Query]
              .build

          val listener = new BookContinousQueryListener
          continousQuery.addContinuousQueryListener(query, listener)

          books.foreach(b => cache.put(b.isbn, b))

          TimeUnit.SECONDS.sleep(1L)

          listener.joiningBooks.map(_.isbn) should be(books.filter(_.price > 4000).map(_.isbn))
        }
      }
    }

    it("delete Entry") {
      withRemoteCacheServer {
        withRemoteCache[String, Book]("namedCache") { cache =>
          registerProtocolBufIdl(cache, classOf[Book])

          val books = Array(
            Book("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 4947),
            Book("978-1785285332", "Getting Started With Hazelcast - Second Edition", 3848),
            Book("978-1783988181", "Mastering Redis", 6172)
          )

          val continousQuery = Search.getContinuousQuery(cache)

          val query =
            Search
              .getQueryFactory(cache)
              .from(classOf[Book])
              .having("price")
              .gte(4000)
              .toBuilder[Query]
              .build

          val listener = new BookContinousQueryListener
          continousQuery.addContinuousQueryListener(query, listener)

          books.foreach(b => cache.put(b.isbn, b))
          books.foreach(b => cache.remove(b.isbn))

          TimeUnit.SECONDS.sleep(1L)

          listener.joiningBooks.map(_.isbn) should be(books.filter(_.price > 4000).map(_.isbn))
          listener.leavingBooks should be(books.filter(_.price > 4000).map(_.isbn))
        }
      }
    }

    it("join Entry") {
      withRemoteCacheServer {
        withRemoteCache[String, Book]("namedCache") { cache =>
          registerProtocolBufIdl(cache, classOf[Book])

          val book = Book("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 4947)

          val continousQuery = Search.getContinuousQuery(cache)

          val query =
            Search
              .getQueryFactory(cache)
              .from(classOf[Book])
              .having("price")
              .gte(5000)
              .toBuilder[Query]
              .build

          val listener = new BookContinousQueryListener
          continousQuery.addContinuousQueryListener(query, listener)

          cache.put(book.isbn, book)

          TimeUnit.SECONDS.sleep(1L)

          listener.joiningBooks should be(empty)

          val bookUpdated = Book("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 5100)

          cache.put(bookUpdated.isbn, bookUpdated)

          TimeUnit.SECONDS.sleep(1L)

          listener.joiningBooks.map(_.isbn) should be(Array(bookUpdated.isbn))
        }
      }
    }

    it("leave Entry") {
      withRemoteCacheServer {
        withRemoteCache[String, Book]("namedCache") { cache =>
          registerProtocolBufIdl(cache, classOf[Book])

          val book = Book("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 4947)

          val continousQuery = Search.getContinuousQuery(cache)

          val query =
            Search
              .getQueryFactory(cache)
              .from(classOf[Book])
              .having("price")
              .gte(4000)
              .toBuilder[Query]
              .build

          val listener = new BookContinousQueryListener
          continousQuery.addContinuousQueryListener(query, listener)

          cache.put(book.isbn, book)

          TimeUnit.SECONDS.sleep(1L)

          listener.joiningBooks.map(_.isbn) should be(Array(book.isbn))

          val bookUpdated = Book("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 3900)

          cache.put(bookUpdated.isbn, bookUpdated)

          TimeUnit.SECONDS.sleep(1L)

          listener.leavingBooks should be(Array(bookUpdated.isbn))
        }
      }
    }
  }

  protected def registerProtocolBufIdl[K, V](cache: RemoteCache[K, V], clazz: Class[_]): Unit = {
    val manager = cache.getRemoteCacheManager

    val context = ProtoStreamMarshaller.getSerializationContext(manager)
    val protoSchemaBuilder = new ProtoSchemaBuilder
    val idl =
      protoSchemaBuilder
        .fileName(clazz.getName)
        .addClass(clazz)
        .build(context)

    val metaCache = manager.getCache[String, String](ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME)
    metaCache.put(clazz.getName + ".proto", idl)
  }

  protected def withRemoteCache[K, V](cacheName: String)(fun: RemoteCache[K, V] => Unit): Unit = {
    val manager = new RemoteCacheManager(
      new ConfigurationBuilder()
        .addServer()
        .host("localhost")
        .port(11222)
        .marshaller(new ProtoStreamMarshaller)
        .build
    )

    try {
      fun(manager.getCache[K, V](cacheName))
    } finally {
      manager.stop()
    }
  }

  protected def withRemoteCacheServer(fun: => Unit): Unit = {
    val embeddedCacheManager = new DefaultCacheManager("infinispan.xml")

    val host = "localhost"
    val port = 11222

    val hotRodServer = new HotRodServer

    try {
      hotRodServer
        .start(
          new HotRodServerConfigurationBuilder()
            .host(host)
            .port(port)
            .build,
          embeddedCacheManager)

      fun
    } finally {
      hotRodServer.stop
      embeddedCacheManager.stop()
    }
  }
}
