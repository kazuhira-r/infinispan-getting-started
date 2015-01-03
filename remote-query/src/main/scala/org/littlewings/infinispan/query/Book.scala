package org.littlewings.infinispan.query

object Book {
  def apply(isbn: String, title: String, price: Int, summary: String): Book = {
    val book = new Book
    book.isbn = isbn
    book.title = title
    book.price = price
    book.summary = summary
    book
  }
}

class Book {
  var isbn: String = _
  var title: String = _
  var price: Int = _
  var summary: String = _
}
