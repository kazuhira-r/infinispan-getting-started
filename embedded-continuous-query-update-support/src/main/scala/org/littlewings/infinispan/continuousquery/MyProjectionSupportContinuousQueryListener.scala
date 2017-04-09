package org.littlewings.infinispan.continuousquery

import org.infinispan.query.api.continuous.ContinuousQueryListener

class MyProjectionSupportContinuousQueryListener extends ContinuousQueryListener[String, Array[AnyRef]] {
  protected[continuousquery] var joined: Int = 0
  protected[continuousquery] var updated: Int = 0
  protected[continuousquery] var leaved: Int = 0

  override def resultJoining(key: String, value: Array[AnyRef]): Unit = {
    synchronized(joined += 1)
    println(s"joined, key = ${key}, value = ${value.mkString(",")}")
  }

  override def resultUpdated(key: String, value: Array[AnyRef]): Unit = {
    synchronized(updated += 1)
    println(s"update, key = ${key}, value = ${value.mkString(",")}")
  }

  override def resultLeaving(key: String): Unit = {
    synchronized(leaved += 1)
    println(s"leaved, key = ${key}")
  }

  def allCount: (Int, Int, Int) = (joined, updated, leaved)
}
