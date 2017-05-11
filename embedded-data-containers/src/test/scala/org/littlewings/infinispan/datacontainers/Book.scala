package org.littlewings.infinispan.datacontainers

object Book {
  def apply(isbn: String, title: String, price: Int): Book =
    new Book(isbn, title, price)
}

@SerialVersionUID(1L)
class Book(val isbn: String, val title: String, val price: Int) extends Serializable
