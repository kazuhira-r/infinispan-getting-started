package org.littlewings.infinispan.icklequery

import org.apache.lucene.analysis.ja.JapaneseAnalyzer
import org.hibernate.search.annotations._

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

@Indexed
@Analyzer(impl = classOf[JapaneseAnalyzer])
@SerialVersionUID(1L)
class IndexedBook extends Serializable {
  @Field(analyze = Analyze.NO)
  @BeanProperty
  var isbn: String = _

  @Field
  @BeanProperty
  var title: String = _

  @Field
  @BeanProperty
  var price: Int = _

  @Field(analyze = Analyze.NO)
  @BeanProperty
  var category: String = _
}
