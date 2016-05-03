package org.littlewings.infinispan.embeddedserver

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

@SerialVersionUID(1L)
class Book extends Serializable {
  @BeanProperty
  var isbn: String = _

  @BeanProperty
  var title: String = _

  @BeanProperty
  var price: Int = _
}
