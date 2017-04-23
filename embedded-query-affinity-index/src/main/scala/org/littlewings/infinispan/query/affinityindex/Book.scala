package org.littlewings.infinispan.query.affinityindex

import org.apache.lucene.analysis.ja.JapaneseAnalyzer
import org.hibernate.search.annotations.{Analyze, Analyzer, Field, Indexed}

import scala.beans.BeanProperty

object Book {
  def apply(isbn: String, title: String, price: Int): Book = {
    val book = new Book
    book.isbn = isbn
    book.title = title
    book.price = price
    book
  }
}

@Indexed
@Analyzer(impl = classOf[JapaneseAnalyzer])
@SerialVersionUID(1L)
class Book extends Serializable {
  @Field(analyze = Analyze.NO)
  @BeanProperty
  var isbn: String = _

  @Field
  @BeanProperty
  var title: String = _

  @Field
  @BeanProperty
  var price: Int = _
}
