package org.littlewings.infinispan.counter

import org.infinispan.counter.api.{CounterEvent, CounterListener, CounterState}

import scala.collection.mutable

class MyCounterListener extends CounterListener {
  val receiveEvents: mutable.Map[Int, (CounterState, Long, CounterState, Long)] =
    mutable.Map.empty[Int, (CounterState, Long, CounterState, Long)]

  override def onUpdate(entry: CounterEvent): Unit = {
    val index = receiveEvents.size + 1
    receiveEvents += (index -> ((entry.getOldState, entry.getOldValue, entry.getNewState, entry.getNewValue)))
    System.out.println(receiveEvents(index))
  }
}
