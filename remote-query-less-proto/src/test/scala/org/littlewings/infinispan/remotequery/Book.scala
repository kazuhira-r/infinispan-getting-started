package org.littlewings.infinispan.remotequery

import org.infinispan.protostream.annotations.{ProtoDoc, ProtoField, ProtoMessage}
import org.infinispan.protostream.descriptors.Type

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

@ProtoDoc("@Indexed")
@ProtoMessage(name = "Book")
class Book {
  var isbn: String = _
  var title: String = _
  var price: Int = _
  var summary: String = _

  @ProtoDoc("@IndexedField")
  @ProtoField(number = 1, name = "isbn", `type` = Type.STRING)
  def getIsbn: String = isbn

  def setIsbn(isbn: String): Unit = this.isbn = isbn

  @ProtoDoc("@IndexedField")
  @ProtoField(number = 2, name = "title", `type` = Type.STRING)
  def getTitle: String = title

  def setTitle(title: String): Unit = this.title = title

  @ProtoDoc("@IndexedField(index = false, store=false)")
  @ProtoField(number = 3, name = "price", `type` = Type.INT32, defaultValue = "0")
  def getPrice: Int = price

  def setPrice(price: Int): Unit = this.price = price

  @ProtoDoc("@IndexedField")
  @ProtoField(number = 4, name = "summary", `type` = Type.STRING)
  def getSummary: String = summary

  def setSummary(summary: String): Unit = this.summary = summary
}
