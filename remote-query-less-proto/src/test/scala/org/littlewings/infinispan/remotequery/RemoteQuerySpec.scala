package org.littlewings.infinispan.remotequery

import org.infinispan.client.hotrod.configuration.ConfigurationBuilder
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller
import org.infinispan.client.hotrod.{RemoteCache, RemoteCacheManager, Search}
import org.infinispan.protostream.annotations.ProtoSchemaBuilder
import org.infinispan.query.dsl.{Query, SortOrder}
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants
import org.scalatest.{FunSpec, Matchers}

class RemoteQuerySpec extends FunSpec with Matchers {
  val sourceSimpleBooks: Seq[SimpleBook] =
    Array(
      SimpleBook("978-4798042169",
        "わかりやすいJavaEEウェブシステム入門",
        3456,
        "JavaEE7準拠。ショッピングサイトや業務システムで使われるJavaEE学習書の決定版!"),
      SimpleBook("978-4798124605",
        "Beginning Java EE 6 GlassFish 3で始めるエンタープライズJava",
        4410,
        "エンタープライズJava入門書の決定版！Java EE 6は、大規模な情報システム構築に用いられるエンタープライズ環境向けのプログラミング言語です。"),
      SimpleBook("978-4774127804",
        "Apache Lucene 入門 ～Java・オープンソース・全文検索システムの構築",
        3200,
        "Luceneは全文検索システムを構築するためのJavaのライブラリです。Luceneを使えば,一味違う高機能なWebアプリケーションを作ることができます。"),
      SimpleBook("978-4774161631",
        "[改訂新版] Apache Solr入門 オープンソース全文検索エンジン",
        3780,
        "最新版Apaceh Solr Ver.4.5.1に対応するため大幅な書き直しと原稿の追加を行い、現在の開発環境に合わせて完全にアップデートしました。Apache Solrは多様なプログラミング言語に対応した全文検索エンジンです。"),
      SimpleBook("978-4048662024",
        "高速スケーラブル検索エンジン ElasticSearch Server",
        3024,
        "Apache Solrを超える全文検索エンジンとして注目を集めるElasticSearch Serverの日本初の解説書です。多くのサンプルを用いた実践的入門書になっています。"),
      SimpleBook("978-1933988177",
        "Lucene in Action",
        6301,
        "New edition of top-selling book on the new version of Lucene. the coreopen-source technology behind most full-text search and Intelligent Web applications.")
    )

  val sourceBooks: Seq[Book] =
    Array(
      Book("978-4798042169",
        "わかりやすいJavaEEウェブシステム入門",
        3456,
        "JavaEE7準拠。ショッピングサイトや業務システムで使われるJavaEE学習書の決定版!"),
      Book("978-4798124605",
        "Beginning Java EE 6 GlassFish 3で始めるエンタープライズJava",
        4410,
        "エンタープライズJava入門書の決定版！Java EE 6は、大規模な情報システム構築に用いられるエンタープライズ環境向けのプログラミング言語です。"),
      Book("978-4774127804",
        "Apache Lucene 入門 ～Java・オープンソース・全文検索システムの構築",
        3200,
        "Luceneは全文検索システムを構築するためのJavaのライブラリです。Luceneを使えば,一味違う高機能なWebアプリケーションを作ることができます。"),
      Book("978-4774161631",
        "[改訂新版] Apache Solr入門 オープンソース全文検索エンジン",
        3780,
        "最新版Apaceh Solr Ver.4.5.1に対応するため大幅な書き直しと原稿の追加を行い、現在の開発環境に合わせて完全にアップデートしました。Apache Solrは多様なプログラミング言語に対応した全文検索エンジンです。"),
      Book("978-4048662024",
        "高速スケーラブル検索エンジン ElasticSearch Server",
        3024,
        "Apache Solrを超える全文検索エンジンとして注目を集めるElasticSearch Serverの日本初の解説書です。多くのサンプルを用いた実践的入門書になっています。"),
      Book("978-1933988177",
        "Lucene in Action",
        6301,
        "New edition of top-selling book on the new version of Lucene. the coreopen-source technology behind most full-text search and Intelligent Web applications.")
    )

  describe("RemoteQuery Spec") {
    it("less .proto file, simple class") {
      withRemoteCache[String, SimpleBook]("indexedCache1") { cache =>
        val thrown = the[IllegalArgumentException] thrownBy sourceSimpleBooks.foreach(b => cache.put(b.isbn, b))
        thrown.getMessage should include("No marshaller registered for class org.littlewings.infinispan.remotequery.SimpleBook")
      }
    }

    it("less .proto file, with annotation class") {
      withRemoteCache[String, Book]("indexedCache2") { cache =>
        val manager = cache.getRemoteCacheManager

        val context = ProtoStreamMarshaller.getSerializationContext(manager)
        val protoSchemaBuilder = new ProtoSchemaBuilder
        val idl = protoSchemaBuilder
          .fileName("/book.proto")
          .addClass(classOf[Book])
          .packageName("remote_query")
          .build(context)

        val metaCache =
          manager.getCache[String, String](ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME)
        metaCache.put("/book.proto", idl)

        sourceBooks.foreach(b => cache.put(b.isbn, b))

        val queryFactory = Search.getQueryFactory(cache)
        val query: Query =
          queryFactory
            .from(classOf[Book])
            .having("title")
            .like("%Java%")
            .and
            .having("title")
            .like("%全文検索%")
            .toBuilder
            .orderBy("price", SortOrder.ASC)
            .build

        query.getResultSize should be(1)

        val books = query.list.asInstanceOf[java.util.List[Book]]

        books should have size 1
        books.get(0).title should be("Apache Lucene 入門 ～Java・オープンソース・全文検索システムの構築")
      }
    }
  }

  protected def withRemoteCache[K, V](cacheName: String)(fun: RemoteCache[K, V] => Unit): Unit = {
    val manager = new RemoteCacheManager(
      new ConfigurationBuilder()
        .addServer
        .host("localhost")
        .port(11222)
        .marshaller(new ProtoStreamMarshaller)
        .build
    )

    val cache = manager.getCache[K, V](cacheName)

    try {
      fun(cache)
      cache.clear()
      cache.stop()
    } finally {
      manager.stop()
    }
  }
}
