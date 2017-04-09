package org.littlewings.infinispan.continuousquery

import org.infinispan.query.api.continuous.ContinuousQueryListener

class MyIndexedContinuousQueryListener extends ContinuousQueryListener[String, IndexedBook] {
  protected[continuousquery] var joined: Int = 0
  protected[continuousquery] var updated: Int = 0
  protected[continuousquery] var leaved: Int = 0

  override def resultJoining(key: String, value: IndexedBook): Unit = {
    synchronized(joined += 1)
    println(s"joined, key = ${key}, value = ${value.title}:${value.price}")
  }

  override def resultUpdated(key: String, value: IndexedBook): Unit = {
    synchronized(updated += 1)
    println(s"update, key = ${key}, value = ${value.title}:${value.price}")
  }

  override def resultLeaving(key: String): Unit = {
    synchronized(leaved += 1)
    println(s"leaved, key = ${key}")
  }

  def allCount: (Int, Int, Int) = (joined, updated, leaved)
}
