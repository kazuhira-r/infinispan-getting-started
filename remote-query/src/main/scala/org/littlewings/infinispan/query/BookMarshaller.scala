package org.littlewings.infinispan.query

import java.io.IOException

import org.infinispan.protostream.MessageMarshaller

class BookMarshaller extends MessageMarshaller[Book] {
  override def getTypeName: String = "remote_query.Book"

  override def getJavaClass: Class[_ <: Book] = classOf[Book]

  @throws(classOf[IOException])
  override def readFrom(reader: MessageMarshaller.ProtoStreamReader): Book = {
    val isbn = reader.readString("isbn")
    val title = reader.readString("title")
    val price = reader.readInt("price")
    val summary = reader.readString("summary")
    Book(isbn, title, price, summary)
  }

  @throws(classOf[IOException])
  override def writeTo(writer: MessageMarshaller.ProtoStreamWriter, book: Book): Unit = {
    writer.writeString("isbn", book.isbn)
    writer.writeString("title", book.title)
    writer.writeInt("price", book.price)
    writer.writeString("summary", book.summary)
  }
}
