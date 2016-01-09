package org.littlewings.infinispan.remotequery

object SimpleBook {
  def apply(isbn: String, title: String, price: Int, summary: String): SimpleBook = {
    val book = new SimpleBook
    book.isbn = isbn
    book.title = title
    book.price = price
    book.summary = summary
    book
  }
}

@SerialVersionUID(1L)
class SimpleBook extends Serializable {
  var isbn: String = _
  var title: String = _
  var price: Int = _
  var summary: String = _
}
