package org.littlewings.infinispan.task

import java.net.InetAddress

import org.infinispan.tasks.{ServerTask, TaskContext, TaskExecutionMode}

class AllNodeSimpleTask extends ServerTask[String] {
  private[task] var taskContext: TaskContext = _

  override def getName: String = "allNodeSimpleTask"

  override def setTaskContext(taskContext: TaskContext): Unit =
    this.taskContext = taskContext

  override def getExecutionMode: TaskExecutionMode = TaskExecutionMode.ALL_NODES

  override def call: String = {
    val cache = taskContext.getCache
    val parameters = taskContext.getParameters

    val name =
      if (parameters.isPresent) parameters.get.get("name").asInstanceOf[String]
      else "Task"

    val cacheName =
      if (cache.isPresent) cache.get.getName
      else "nonCache"

    s"Hello ${name}!!, Cache[${cacheName}] by ${InetAddress.getLocalHost.getHostName}"
  }
}
