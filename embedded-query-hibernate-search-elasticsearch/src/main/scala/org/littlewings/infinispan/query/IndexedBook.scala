package org.littlewings.infinispan.query

import org.hibernate.search.annotations._
import org.hibernate.search.elasticsearch.analyzer.{ElasticsearchTokenFilterFactory, ElasticsearchTokenizerFactory}

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

@Indexed(index = "book")
@AnalyzerDef(
  name = "kuromoji_analyzer",
  tokenizer = new TokenizerDef(
    factory = classOf[ElasticsearchTokenizerFactory],
    params = Array(new Parameter(name = "type", value = "kuromoji_tokenizer"))
  ),
  filters = Array(
    new TokenFilterDef(
      name = "kuromoji_baseform",
      factory = classOf[ElasticsearchTokenFilterFactory],
      params = Array(new Parameter(name = "type", value = "kuromoji_baseform"))
    ),
    new TokenFilterDef(
      name = "kuromoji_part_of_speech",
      factory = classOf[ElasticsearchTokenFilterFactory],
      params = Array(new Parameter(name = "type", value = "kuromoji_part_of_speech"))
    ),
    new TokenFilterDef(
      name = "cjk_width",
      factory = classOf[ElasticsearchTokenFilterFactory],
      params = Array(new Parameter(name = "type", value = "cjk_width"))
    ),
    new TokenFilterDef(
      name = "stop",
      factory = classOf[ElasticsearchTokenFilterFactory],
      params = Array(new Parameter(name = "type", value = "stop"))
    ),
    new TokenFilterDef(
      name = "ja_stop",
      factory = classOf[ElasticsearchTokenFilterFactory],
      params = Array(new Parameter(name = "type", value = "ja_stop"))
    ),
    new TokenFilterDef(
      name = "kuromoji_stemmer",
      factory = classOf[ElasticsearchTokenFilterFactory],
      params = Array(new Parameter(name = "type", value = "kuromoji_stemmer"))
    ),
    new TokenFilterDef(
      name = "lowercase",
      factory = classOf[ElasticsearchTokenFilterFactory],
      params = Array(new Parameter(name = "type", value = "lowercase"))
    )
  )
)
@Analyzer(definition = "kuromoji_analyzer")
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
