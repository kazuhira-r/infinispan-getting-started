package org.littlewings.infinispan.task

import org.infinispan.Cache
import org.infinispan.tasks.{ServerTask, TaskContext}

class CacheSimpleTask extends ServerTask[String] {
  private[task] var taskContext: TaskContext = _

  override def getName: String = "cacheSimpleTask"

  override def setTaskContext(taskContext: TaskContext): Unit =
    this.taskContext = taskContext

  override def call: String = {
    val parameters = taskContext.getParameters.get
    val marshaller = taskContext.getMarshaller.get
    val cache = taskContext.getCache.get.asInstanceOf[Cache[Array[Byte], Array[Byte]]]

    val key = parameters.get("key")

    val keyAsBinary = marshaller.objectToByteBuffer(key)
    val value = marshaller.objectFromByteBuffer(cache.get(keyAsBinary))

    s"key = ${key}, value = ${value}"
  }
}
