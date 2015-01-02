package org.littlewings.infinispan.query

import scala.beans.BeanProperty

object Book {
  def apply(isbn: String, title: String, price: Int, summary: String): Book = {
    val book = new Book
    book.isbn = isbn
    book.title= title
    book.price = price
    book.summary = summary
    book
  }
}

@SerialVersionUID(1L)
class Book extends Serializable {
  @BeanProperty
  var isbn: String = _

  @BeanProperty
  var title: String = _

  @BeanProperty
  var price: Int = _

  @BeanProperty
  var summary: String = _
}
