package org.littlewings.infinispan.task

import org.infinispan.Cache
import org.infinispan.tasks.{ServerTask, TaskContext}
import org.littlewings.infinispan.task.entity.Book

class CompatibilityBookPriceDoublingTask extends ServerTask[Book] {
  private[task] var taskContext: TaskContext = _

  override def getName: String = "compatibilityBookPriceDoublingTask"

  override def setTaskContext(taskContext: TaskContext): Unit =
    this.taskContext = taskContext

  override def call: Book = {
    val parameters = taskContext.getParameters.get
    val marshaller = taskContext.getMarshaller.get
    val cache = taskContext.getCache.get.asInstanceOf[Cache[String, Array[Byte]]]

    // parameter
    val parameterBook = parameters.get("target").asInstanceOf[Book]

    // query
    val isbn = parameterBook.getIsbn
    val cacheBook = marshaller.objectFromByteBuffer(cache.get(isbn)).asInstanceOf[Book]

    // new
    new Book(cacheBook.getIsbn, cacheBook.getTitle, cacheBook.getPrice * 2)
  }
}
