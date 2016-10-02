package org.littlewings.infinispan.remotecq

import org.infinispan.protostream.annotations.{ProtoField, ProtoMessage}
import org.infinispan.protostream.descriptors.Type

object Book {
  def apply(isbn: String, title: String, price: Int): Book = {
    val book = new Book

    book.isbn = isbn
    book.title = title
    book.price = price

    book
  }
}

@ProtoMessage(name = "Book")
class Book extends Serializable {
  private[remotecq] var isbn: String = _
  private[remotecq] var title: String = _
  private[remotecq] var price: Int = _

  @ProtoField(number = 1, name = "isbn", required = true, `type` = Type.STRING)
  def getIsbn: String = isbn

  def setIsbn(isbn: String): Unit = this.isbn = isbn

  @ProtoField(number = 2, name = "title", required = true, `type` = Type.STRING)
  def getTitle: String = title

  def setTitle(title: String): Unit = this.title = title

  @ProtoField(number = 3, name = "price", required = true, defaultValue = "0", `type` = Type.INT32)
  def getPrice: Int = price

  def setPrice(price: Int): Unit = this.price = price
}
