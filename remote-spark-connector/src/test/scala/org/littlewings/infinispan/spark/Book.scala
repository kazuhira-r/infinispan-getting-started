package org.littlewings.infinispan.spark

import org.infinispan.protostream.annotations.{ProtoDoc, ProtoField, ProtoMessage}
import org.infinispan.protostream.descriptors.Type

object Book {
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

  def apply(isbn: String, title: String, price: Int, summary: String): Book = {
    val book = new Book
    book.isbn = isbn
    book.title = title
    book.price = price
    book.summary = summary
    book
  }
}

@ProtoDoc("@Indexed")
@ProtoMessage(name = "Book")
class Book {
  var isbn: String = _
  var title: String = _
  var price: Int = _
  var summary: String = _

  @ProtoDoc("@IndexedField")
  @ProtoField(number = 1, name = "isbn", `type` = Type.STRING)
  def getIsbn: String = isbn

  def setIsbn(isbn: String): Unit = this.isbn = isbn

  @ProtoDoc("@IndexedField")
  @ProtoField(number = 2, name = "title", `type` = Type.STRING)
  def getTitle: String = title

  def setTitle(title: String): Unit = this.title = title

  @ProtoDoc("@IndexedField")
  @ProtoField(number = 3, name = "price", required = true, defaultValue = "0")
  def getPrice: Int = price

  def setPrice(price: Int): Unit = this.price = price

  @ProtoDoc("@IndexedField")
  @ProtoField(number = 4, name = "summary", `type` = Type.STRING)
  def getSummary: String = summary

  def setSummary(summary: String): Unit = this.summary = summary
}
