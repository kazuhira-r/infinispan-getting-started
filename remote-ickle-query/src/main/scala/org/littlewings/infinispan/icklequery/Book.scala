package org.littlewings.infinispan.icklequery

import org.infinispan.protostream.annotations.{ProtoField, ProtoMessage}
import org.infinispan.protostream.descriptors.Type

import scala.annotation.meta.beanGetter
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

@ProtoMessage(name = "Book")
class Book {
  @BeanProperty
  @(ProtoField @beanGetter)(number = 1, name = "isbn", `type` = Type.STRING)
  var isbn: String = _

  @BeanProperty
  @(ProtoField @beanGetter)(number = 2, name = "title", `type` = Type.STRING)
  var title: String = _

  @BeanProperty
  @(ProtoField @beanGetter)(number = 3, name = "price", `type` = Type.INT32, defaultValue = "0")
  var price: Int = _

  @BeanProperty
  @(ProtoField @beanGetter)(number = 4, name = "category", `type` = Type.STRING)
  var category: String = _
}
