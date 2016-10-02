package org.littlewings.infinispan.remotecq

import org.infinispan.query.api.continuous.ContinuousQueryListener

class BookContinousQueryListener extends ContinuousQueryListener[String, Book] {
  var joiningBooks: Vector[Book] = Vector.empty
  var leavingBooks: Vector[String] = Vector.empty

  override def resultJoining(key: String, value: Book): Unit = {
    joiningBooks = joiningBooks :+ value
  }

  override def resultLeaving(key: String): Unit =
    leavingBooks = leavingBooks :+ key
}
