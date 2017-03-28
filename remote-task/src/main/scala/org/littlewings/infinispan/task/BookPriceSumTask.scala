package org.littlewings.infinispan.task

import java.util.stream.{Collector, Collectors}

import org.infinispan.Cache
import org.infinispan.stream.{CacheCollectors, SerializableSupplier}
import org.infinispan.tasks.{ServerTask, TaskContext}
import org.littlewings.infinispan.task.entity.Book

class BookPriceSumTask extends ServerTask[Int] {
  private[task] var taskContext: TaskContext = _

  override def getName: String = "bookPriceSumTask"

  override def setTaskContext(taskContext: TaskContext): Unit =
    this.taskContext = taskContext

  override def call: Int = {
    val cache = taskContext.getCache.get.asInstanceOf[Cache[String, Book]]

    val stream = cache.entrySet.stream

    try {
      stream.map[Int](new java.util.function.Function[java.util.Map.Entry[String, Book], Int] with Serializable {
        override def apply(entity: java.util.Map.Entry[String, Book]): Int = entity.getValue.getPrice
      }).collect(CacheCollectors.serializableCollector[Int, Integer](new SerializableSupplier[Collector[Int, _, Integer]] {
        override def get: Collector[Int, _, Integer] = Collectors.summingInt[Int](i => Integer.valueOf(i))
      }))
    } finally {
      stream.close()
    }
  }
}
