package org.littlewings.infinispan.query

import scala.beans.BeanProperty

import org.hibernate.search.annotations.{ Analyze, Field, Indexed }

object FullTextIndexingBook {
  def apply(isbn: String, title: String, price: Int, summary: String): FullTextIndexingBook = {
    val book = new FullTextIndexingBook
    book.isbn = isbn
    book.title= title
    book.price = price
    book.summary = summary
    book
  }
}

@Indexed
@SerialVersionUID(1L)
class FullTextIndexingBook extends Serializable {
  @Field(analyze = Analyze.NO)
  @BeanProperty
  var isbn: String = _

  @Field
  @BeanProperty
  var title: String = _

  @Field(analyze = Analyze.NO)
  @BeanProperty
  var price: Int = _

  @Field
  @BeanProperty
  var summary: String = _
}
