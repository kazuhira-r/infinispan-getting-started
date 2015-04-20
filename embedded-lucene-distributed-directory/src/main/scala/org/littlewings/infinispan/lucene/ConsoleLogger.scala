package org.littlewings.infinispan.lucene

trait ConsoleLogger {
  val nodeName: String

  def log(msg: => String): Unit = println(s"$nodeName> $msg")

  def show(msg: => String): Unit = println(msg)
}
