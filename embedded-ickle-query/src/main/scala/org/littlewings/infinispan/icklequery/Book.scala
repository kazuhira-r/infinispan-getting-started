package org.littlewings.infinispan.icklequery

import scala.beans.BeanProperty

object Book {
  def apply(isbn: String, title: String, price: Int, category: String): Book = {
    val book = new Book
    book.isbn = isbn
    book.title = title
    book.price = price
    book.category = category
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
  var category: String = _
}
