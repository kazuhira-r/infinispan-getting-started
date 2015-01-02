package org.littlewings.infinispan.query

import org.infinispan.query.Search
import org.infinispan.query.dsl.{ Query, SortOrder }

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class WithIndexingQueryDslSpec extends FunSpec with InfinispanClusteredSpecSupport {
  private def sourceBooks: Seq[NonAnalyzedBook] =
    Array(
      NonAnalyzedBook("978-4798042169",
        "わかりやすいJavaEEウェブシステム入門",
        3456,
        "JavaEE7準拠。ショッピングサイトや業務システムで使われるJavaEE学習書の決定版!"),
      NonAnalyzedBook("978-4798124605",
        "Beginning Java EE 6 GlassFish 3で始めるエンタープライズJava",
        4410,
        "エンタープライズJava入門書の決定版！Java EE 6は、大規模な情報システム構築に用いられるエンタープライズ環境向けのプログラミング言語です。"),
      NonAnalyzedBook("978-4774127804",
        "Apache Lucene 入門 ～Java・オープンソース・全文検索システムの構築",
        3200,
        "Luceneは全文検索システムを構築するためのJavaのライブラリです。Luceneを使えば,一味違う高機能なWebアプリケーションを作ることができます。"),
      NonAnalyzedBook("978-4774161631",
        "[改訂新版] Apache Solr入門 オープンソース全文検索エンジン",
        3780,
        "最新版Apaceh Solr Ver.4.5.1に対応するため大幅な書き直しと原稿の追加を行い、現在の開発環境に合わせて完全にアップデートしました。Apache Solrは多様なプログラミング言語に対応した全文検索エンジンです。"),
      NonAnalyzedBook("978-4048662024",
        "高速スケーラブル検索エンジン ElasticSearch Server",
        3024,
        "Apache Solrを超える全文検索エンジンとして注目を集めるElasticSearch Serverの日本初の解説書です。多くのサンプルを用いた実践的入門書になっています。"),
      NonAnalyzedBook("978-1933988177",
        "Lucene in Action",
        6301,
        "New edition of top-selling book on the new version of Lucene. the coreopen-source technology behind most full-text search and Intelligent Web applications.")
    )

  describe("Infinispan With Indexing Query DSL") {
    it("Build Query") {
      withCache[String, NonAnalyzedBook](3, "infinispan-query-dsl.xml", "indexingCache") { cache =>
        sourceBooks.foreach(b => cache.put(b.isbn, b))

        val queryFactory = Search.getQueryFactory(cache)

        val query: Query =
          queryFactory
            .from(classOf[NonAnalyzedBook])
            .having("title")
            .like("%Java%")
            .and
            .having("title")
            .like("%全文検索%")
            .toBuilder
            .orderBy("price", SortOrder.ASC)
            .build

        query.toString should include ("query=+title:*Java* +title:*全文検索*")
      }
    }

    it("Search") {
      withCache[String, NonAnalyzedBook](3, "infinispan-query-dsl.xml", "indexingCache") { cache =>
        sourceBooks.foreach(b => cache.put(b.isbn, b))

        val queryFactory = Search.getQueryFactory(cache)

        val query: Query =
          queryFactory
            .from(classOf[NonAnalyzedBook])
            .having("title")
            .like("%Java%")
            .and
            .having("title")
            .like("%全文検索%")
            .toBuilder
            .orderBy("price", SortOrder.ASC)
            .build

        query.getResultSize should be (1)

        val books = query.list.asInstanceOf[java.util.List[NonAnalyzedBook]]

        books should have size 1
        books.get(0).title should be ("Apache Lucene 入門 ～Java・オープンソース・全文検索システムの構築")
      }
    }
  }
}
