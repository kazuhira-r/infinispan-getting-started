package org.littlewings.infinispan.query

import scala.beans.BeanProperty

import org.hibernate.search.annotations.{ Analyze, Field, Indexed }

object NonAnalyzedBook {
  def apply(isbn: String, title: String, price: Int, summary: String): NonAnalyzedBook = {
    val book = new NonAnalyzedBook
    book.isbn = isbn
    book.title= title
    book.price = price
    book.summary = summary
    book
  }
}

@Indexed
@SerialVersionUID(1L)
class NonAnalyzedBook extends Serializable {
  @Field(analyze = Analyze.NO)
  @BeanProperty
  var isbn: String = _

  @Field(analyze = Analyze.NO)
  @BeanProperty
  var title: String = _

  @Field(analyze = Analyze.NO)
  @BeanProperty
  var price: Int = _

  @Field(analyze = Analyze.NO)
  @BeanProperty
  var summary: String = _
}
