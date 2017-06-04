package org.littlewings.infinispan.icklequery

import org.infinispan.protostream.annotations.{ProtoDoc, ProtoField, ProtoMessage}
import org.infinispan.protostream.descriptors.Type

import scala.annotation.meta.beanGetter
import scala.beans.BeanProperty

object IndexedBook {
  def apply(isbn: String, title: String, price: Int, category: String): IndexedBook = {
    val book = new IndexedBook
    book.isbn = isbn
    book.title = title
    book.price = price
    book.category = category
    book
  }
}

@ProtoDoc("@Indexed")
@ProtoMessage(name = "IndexedBook")
class IndexedBook {
  @BeanProperty
  @(ProtoDoc @beanGetter)("@Field")
  @(ProtoField @beanGetter)(number = 1, name = "isbn", `type` = Type.STRING)
  var isbn: String = _

  @BeanProperty
  @(ProtoDoc @beanGetter)("@Field(analyze = Analyze.YES)")
  @(ProtoField @beanGetter)(number = 2, name = "title", `type` = Type.STRING)
  var title: String = _

  @BeanProperty
  @(ProtoDoc @beanGetter)("@Field")
  @(ProtoField @beanGetter)(number = 3, name = "price", `type` = Type.INT32, defaultValue = "0")
  var price: Int = _

  @BeanProperty
  @(ProtoDoc @beanGetter)("@Field")
  @(ProtoField @beanGetter)(number = 4, name = "category", `type` = Type.STRING)
  var category: String = _
}
