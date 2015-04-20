package org.littlewings.infinispan.lucene

import org.infinispan.commons.api.Lifecycle

object ResourceWrappers {
  implicit class LifecycleWrapper[A <: Lifecycle](val underlying: A) extends AnyVal {
    def foreach(f: A => Unit): Unit =
      try {
        f(underlying)
      } finally {
        underlying.stop()
      }
  }

  implicit class AutoClosebleWrapper[A <: AutoCloseable](val underlying: A) extends AnyVal {
    def foreach(f: A => Unit): Unit =
      try {
        f(underlying)
      } finally {
        underlying.close()
      }
  }
}
