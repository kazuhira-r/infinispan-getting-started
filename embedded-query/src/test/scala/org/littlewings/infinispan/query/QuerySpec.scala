package org.littlewings.infinispan.query

import org.apache.lucene.search.{ Sort, SortField }
import org.infinispan.query.Search

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class QuerySpec extends FunSpec with InfinispanClusteredSpecSupport {
  private def sourceBooks: Seq[FullTextIndexingBook] =
    Array(
      FullTextIndexingBook("978-4798042169",
        "わかりやすいJavaEEウェブシステム入門",
        3456,
        "JavaEE7準拠。ショッピングサイトや業務システムで使われるJavaEE学習書の決定版!"),
      FullTextIndexingBook("978-4798124605",
        "Beginning Java EE 6 GlassFish 3で始めるエンタープライズJava",
        4410,
        "エンタープライズJava入門書の決定版！Java EE 6は、大規模な情報システム構築に用いられるエンタープライズ環境向けのプログラミング言語です。"),
      FullTextIndexingBook("978-4774127804",
        "Apache Lucene 入門 ～Java・オープンソース・全文検索システムの構築",
        3200,
        "Luceneは全文検索システムを構築するためのJavaのライブラリです。Luceneを使えば,一味違う高機能なWebアプリケーションを作ることができます。"),
      FullTextIndexingBook("978-4774161631",
        "[改訂新版] Apache Solr入門 オープンソース全文検索エンジン",
        3780,
        "最新版Apaceh Solr Ver.4.5.1に対応するため大幅な書き直しと原稿の追加を行い、現在の開発環境に合わせて完全にアップデートしました。Apache Solrは多様なプログラミング言語に対応した全文検索エンジンです。"),
      FullTextIndexingBook("978-4048662024",
        "高速スケーラブル検索エンジン ElasticSearch Server",
        3024,
        "Apache Solrを超える全文検索エンジンとして注目を集めるElasticSearch Serverの日本初の解説書です。多くのサンプルを用いた実践的入門書になっています。"),
      FullTextIndexingBook("978-1933988177",
        "Lucene in Action",
        6301,
        "New edition of top-selling book on the new version of Lucene. the coreopen-source technology behind most full-text search and Intelligent Web applications.")
    )

  describe("Infinispan Query Spec") {
    it("Buid Full Text Query") {
      withCache[String, FullTextIndexingBook](3, "infinispan-query.xml", "indexingCache") { cache =>
        sourceBooks.foreach(b => cache.put(b.isbn, b))

        val searchManager = Search.getSearchManager(cache)
        val queryBuilder = searchManager.buildQueryBuilderForClass(classOf[FullTextIndexingBook]).get

        val fullTextQuery =
          queryBuilder
            .keyword
            .onFields("title", "summary")
            .matching("全文検索 Java 日本語")
            .createQuery

        fullTextQuery.toString should be ("(title:全文 title:検索 title:java title:日本 title:日本語 title:語) (summary:全文 summary:検索 summary:java summary:日本 summary:日本語 summary:語)")
      }
    }

    it("Search") {
      withCache[String, FullTextIndexingBook](3, "infinispan-query.xml", "indexingCache") { cache =>
        sourceBooks.foreach(b => cache.put(b.isbn, b))

        val searchManager = Search.getSearchManager(cache)
        val queryBuilder = searchManager.buildQueryBuilderForClass(classOf[FullTextIndexingBook]).get

        val fullTextQuery =
          queryBuilder
            .keyword
            .onFields("title", "summary")
            .matching("全文検索 Java 日本語")
            .createQuery

        val cacheQuery = searchManager.getQuery(fullTextQuery, classOf[FullTextIndexingBook])

        val books =
          cacheQuery
            .sort(new Sort(new SortField("price", SortField.Type.INT)))
            .list
            .asInstanceOf[java.util.List[FullTextIndexingBook]]

        books should have size 4

        books.get(0).title should be ("高速スケーラブル検索エンジン ElasticSearch Server")
        books.get(1).title should be ("Apache Lucene 入門 ～Java・オープンソース・全文検索システムの構築")
        books.get(2).title should be ("[改訂新版] Apache Solr入門 オープンソース全文検索エンジン")
        books.get(3).title should be ("Beginning Java EE 6 GlassFish 3で始めるエンタープライズJava")
      }
    }
  }
}
