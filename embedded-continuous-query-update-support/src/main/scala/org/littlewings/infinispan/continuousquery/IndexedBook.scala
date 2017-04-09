package org.littlewings.infinispan.continuousquery

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.hibernate.search.annotations._

import scala.beans.BeanProperty

object IndexedBook {
  def apply(isbn: String, title: String, price: Int): IndexedBook = {
    val book = new IndexedBook
    book.isbn = isbn
    book.title = title
    book.price = price
    book
  }
}

@Indexed
@Analyzer(impl = classOf[StandardAnalyzer])
@SerialVersionUID(1L)
class IndexedBook extends Serializable {
  @Field(analyze = Analyze.NO)
  @BeanProperty
  var isbn: String = _

  @Field
  @BeanProperty
  var title: String = _

  @Field
  @BeanProperty
  var price: Int = _

  def newBookChangePrice(newPrice: Int): IndexedBook = {
    val book = new IndexedBook
    book.isbn = isbn
    book.title = title
    book.price = newPrice
    book
  }
}
