package org.littlewings.infinispan.counter

import java.util.concurrent.CompletionException
import java.util.stream.Collectors

import org.infinispan.counter.EmbeddedCounterManagerFactory
import org.infinispan.counter.api.CounterState
import org.infinispan.counter.exception.CounterOutOfBoundsException
import org.infinispan.counter.impl.CounterModuleLifecycle
import org.infinispan.counter.impl.entries.{CounterKey, CounterValue}
import org.infinispan.counter.impl.strong.{BoundedStrongCounter, UnboundedStrongCounter}
import org.infinispan.counter.impl.weak.WeakCounterImpl
import org.infinispan.manager.{DefaultCacheManager, EmbeddedCacheManager}
import org.infinispan.util.function.{SerializableFunction, SerializablePredicate}
import org.scalatest.{FunSuite, Matchers}

class ClusteredCounterSpec extends FunSuite with Matchers {
  test("simple strong-counter") {
    withCacheManagers("infinispan-counter-volatile.xml", 3) { managers =>
      val manager = managers(0)
      val counterManager = EmbeddedCounterManagerFactory.asCounterManager(manager)
      val strongCounter = counterManager.getStrongCounter("simpleStrongCounter")

      strongCounter.getName should be("simpleStrongCounter")
      strongCounter.getValue.join() should be(0L)
      strongCounter.incrementAndGet().join() should be(1L)
      strongCounter.addAndGet(100L).join() should be(101L)
      strongCounter.decrementAndGet().join() should be(100L)
      strongCounter.compareAndSet(100L, 120L)

      EmbeddedCounterManagerFactory
        .asCounterManager(managers(1))
        .getStrongCounter("simpleStrongCounter")
        .getValue.join() should be(120L)

      EmbeddedCounterManagerFactory
        .asCounterManager(managers(2))
        .getStrongCounter("simpleStrongCounter")
        .addAndGet(50L).join()

      strongCounter.getValue.join() should be(170L)

      strongCounter.reset().join()
      strongCounter.getValue.join() should be(0L)

      strongCounter.addAndGet(200L).join()

      strongCounter should be(a[UnboundedStrongCounter])

      val countersCache = manager.getCache[CounterKey, CounterValue](CounterModuleLifecycle.COUNTER_CACHE_NAME)
      countersCache should have size 1
      val counterKey = countersCache.keySet.stream().filter(new SerializablePredicate[CounterKey] {
        override def test(key: CounterKey): Boolean = key.getCounterName.toString == "simpleStrongCounter"
      }).findFirst().get

      countersCache.get(counterKey).getValue should be(200L)
    }
  }

  test("simple weak-counter") {
    withCacheManagers("infinispan-counter-volatile.xml", 3) { managers =>
      val manager = managers(0)
      val counterManager = EmbeddedCounterManagerFactory.asCounterManager(manager)
      val weakCounter = counterManager.getWeakCounter("simpleWeakCounter")

      weakCounter.getName should be("simpleWeakCounter")
      weakCounter.getValue should be(0L)
      weakCounter.increment().join()
      weakCounter.getValue should be(1L)
      weakCounter.add(150L).join()
      weakCounter.getValue should be(151L)
      weakCounter.decrement().join()
      weakCounter.getValue should be(150L)

      EmbeddedCounterManagerFactory
        .asCounterManager(managers(1))
        .getWeakCounter("simpleWeakCounter")
        .getValue should be(150L)

      EmbeddedCounterManagerFactory
        .asCounterManager(managers(2))
        .getWeakCounter("simpleWeakCounter")
        .add(100L).join()

      weakCounter.getValue should be(250L)

      weakCounter.reset().join()
      weakCounter.getValue should be(0L)

      weakCounter.add(100L).join()

      EmbeddedCounterManagerFactory
        .asCounterManager(managers(1))
        .getWeakCounter("simpleWeakCounter")
        .add(50).join()

      EmbeddedCounterManagerFactory
        .asCounterManager(managers(2))
        .getWeakCounter("simpleWeakCounter")
        .add(200L).join()

      weakCounter.getValue should be(350L)

      weakCounter should be(a[WeakCounterImpl])

      val countersCache = manager.getCache[CounterKey, CounterValue](CounterModuleLifecycle.COUNTER_CACHE_NAME)
      countersCache should have size 6

      val keys = countersCache.keySet().stream().map[String](new SerializableFunction[CounterKey, String] {
        override def apply(key: CounterKey): String = key.toString
      }).sorted().collect(Collectors.toList[String])

      keys should have size 6
      (0 until 6).foreach(i => keys.get(i) should be(s"WeakCounterKey{counterName=simpleWeakCounter, index=${i}}"))
    }
  }

  test("initial-valued strong-counter") {
    withCacheManagers("infinispan-counter-volatile.xml", 3) { managers =>
      val manager = managers(0)
      val counterManager = EmbeddedCounterManagerFactory.asCounterManager(manager)
      val strongCounter = counterManager.getStrongCounter("initialValuedStrongCounter")

      strongCounter.getValue.join() should be(100L)
      strongCounter.addAndGet(50L).join() should be(150L)

      strongCounter.reset().join()
      strongCounter.getValue.join() should be(100L)
    }
  }

  test("initial-valued weak-counter") {
    withCacheManagers("infinispan-counter-volatile.xml", 3) { managers =>
      val manager = managers(0)
      val counterManager = EmbeddedCounterManagerFactory.asCounterManager(manager)
      val weakCounter = counterManager.getWeakCounter("initialValuedWeakCounter")

      weakCounter.getValue should be(100L)
      weakCounter.add(50L).join()
      weakCounter.getValue should be(150L)

      weakCounter.reset().join()
      weakCounter.getValue should be(100L)
    }
  }

  test("bounded strong-counter") {
    withCacheManagers("infinispan-counter-volatile.xml", 3) { managers =>
      val manager = managers(0)
      val counterManager = EmbeddedCounterManagerFactory.asCounterManager(manager)
      val strongCounter = counterManager.getStrongCounter("boundedStrongCounter")

      strongCounter.getValue.join() should be(100L)
      strongCounter.addAndGet(50L).join() should be(150L)

      val thrown = the[CompletionException] thrownBy strongCounter.incrementAndGet().join()
      val counterOutOfBounds = thrown.getCause.asInstanceOf[CounterOutOfBoundsException]
      counterOutOfBounds should be(a[CounterOutOfBoundsException])
      counterOutOfBounds.getMessage should be("ISPN028001: Upper bound reached.")
      counterOutOfBounds.isUpperBoundReached should be(true)

      strongCounter.reset().join()
      strongCounter.getValue.join() should be(100L)

      strongCounter should be(a[BoundedStrongCounter])
    }
  }

  test("tuned concurrency-level weak-counter") {
    withCacheManagers("infinispan-counter-volatile.xml", 3) { managers =>
      val manager = managers(0)
      val counterManager = EmbeddedCounterManagerFactory.asCounterManager(manager)
      val weakCounter = counterManager.getWeakCounter("tunedConcurrencyLevelWeakCounter")

      weakCounter.getValue should be(0L)
      weakCounter.increment().join()
      weakCounter.getValue should be(1L)
      weakCounter.increment().join()
      weakCounter.getValue should be(2L)

      val countersCache = manager.getCache[CounterKey, CounterValue]("___counters")
      countersCache should have size 8

      val keys = countersCache.keySet().stream().map[String](new SerializableFunction[CounterKey, String] {
        override def apply(key: CounterKey): String = key.toString
      }).sorted().collect(Collectors.toList[String])

      keys should have size 8
      (0 until 8).foreach(i => keys.get(i) should be(s"WeakCounterKey{counterName=tunedConcurrencyLevelWeakCounter, index=${i}}"))
    }
  }

  test("volatile strong-counter") {
    withCacheManagers("infinispan-counter-volatile.xml", 1) { managers =>
      val manager = managers(0)
      val counterManager = EmbeddedCounterManagerFactory.asCounterManager(manager)
      val strongCounter = counterManager.getStrongCounter("simpleStrongCounter")

      strongCounter.getValue.join() should be(0L)
      strongCounter.addAndGet(100L).join() should be(100L)
    }

    withCacheManagers("infinispan-counter-volatile.xml", 1) { managers =>
      val manager = managers(0)
      val counterManager = EmbeddedCounterManagerFactory.asCounterManager(manager)
      val strongCounter = counterManager.getStrongCounter("simpleStrongCounter")

      strongCounter.getValue.join() should be(0L)
    }
  }

  test("persistent strong-counter") {
    withCacheManagers("infinispan-counter-persistent.xml", 1) { managers =>
      val manager = managers(0)
      val counterManager = EmbeddedCounterManagerFactory.asCounterManager(manager)
      val strongCounter = counterManager.getStrongCounter("persistentStrongCounter")

      strongCounter.getValue.join() should be(0L)
      strongCounter.addAndGet(100L).join() should be(100L)
    }

    withCacheManagers("infinispan-counter-persistent.xml", 1) { managers =>
      val manager = managers(0)
      val counterManager = EmbeddedCounterManagerFactory.asCounterManager(manager)
      val strongCounter = counterManager.getStrongCounter("persistentStrongCounter")

      strongCounter.getValue.join() should be(100L)
    }
  }

  test("volatile weak-counter") {
    withCacheManagers("infinispan-counter-volatile.xml", 1) { managers =>
      val manager = managers(0)
      val counterManager = EmbeddedCounterManagerFactory.asCounterManager(manager)
      val weakCounter = counterManager.getWeakCounter("simpleWeakCounter")

      weakCounter.getValue should be(0L)
      weakCounter.add(100L).join()
    }

    withCacheManagers("infinispan-counter-volatile.xml", 1) { managers =>
      val manager = managers(0)
      val counterManager = EmbeddedCounterManagerFactory.asCounterManager(manager)
      val weakCounter = counterManager.getWeakCounter("simpleWeakCounter")

      weakCounter.getValue should be(0L)
    }
  }

  test("persistent weak-counter") {
    withCacheManagers("infinispan-counter-persistent.xml", 1) { managers =>
      val manager = managers(0)
      val counterManager = EmbeddedCounterManagerFactory.asCounterManager(manager)
      val weakCounter = counterManager.getWeakCounter("persistentWeakCounter")

      weakCounter.getValue should be(0L)
      weakCounter.add(100L).join()
    }

    withCacheManagers("infinispan-counter-persistent.xml", 1) { managers =>
      val manager = managers(0)
      val counterManager = EmbeddedCounterManagerFactory.asCounterManager(manager)
      val weakCounter = counterManager.getWeakCounter("persistentWeakCounter")

      weakCounter.getValue should be(100L)
    }
  }

  test("bounded strong-counter with listener") {
    withCacheManagers("infinispan-counter-volatile.xml", 3) { managers =>
      val manager = managers(0)
      val counterManager = EmbeddedCounterManagerFactory.asCounterManager(manager)
      val strongCounter = counterManager.getStrongCounter("boundedStrongCounter")

      val listener = new MyCounterListener
      val handle = strongCounter.addListener(listener)

      strongCounter.getValue.join() should be(100L)
      strongCounter.addAndGet(50L).join() should be(150L)

      listener.receiveEvents(1) should be((CounterState.VALID, 100L, CounterState.VALID, 150L))

      val thrown = the[CompletionException] thrownBy strongCounter.incrementAndGet().join()
      val counterOutOfBounds = thrown.getCause.asInstanceOf[CounterOutOfBoundsException]
      counterOutOfBounds.getMessage should be("ISPN028001: Upper bound reached.")

      listener.receiveEvents(2) should be((CounterState.VALID, 150L, CounterState.UPPER_BOUND_REACHED, 150L))

      strongCounter.reset().join()

      listener.receiveEvents(3) should be((CounterState.UPPER_BOUND_REACHED, 150L, CounterState.VALID, 100L))

      strongCounter.getValue.join() should be(100L)

      listener.receiveEvents should  have size 3

      handle.getCounterListener should be(listener)
      handle.remove()
    }
  }

  test("weak-counter with listener") {
    withCacheManagers("infinispan-counter-volatile.xml", 3) { managers =>
      val manager = managers(0)
      val counterManager = EmbeddedCounterManagerFactory.asCounterManager(manager)
      val weakCounter = counterManager.getWeakCounter("initialValuedWeakCounter")

      val listener = new MyCounterListener
      val handle = weakCounter.addListener(listener)

      weakCounter.getValue should be(100L)

      weakCounter.increment().join()
      listener.receiveEvents(1) should be((CounterState.VALID, 100L, CounterState.VALID, 101L))

      weakCounter.increment().join()
      listener.receiveEvents(2) should be((CounterState.VALID, 101L, CounterState.VALID, 102L))

      weakCounter.reset().join()
      listener.receiveEvents(3) should be((CounterState.VALID, 102L, CounterState.VALID, 102L))
      listener.receiveEvents(4) should be((CounterState.VALID, 102L, CounterState.VALID, 100L))
      listener.receiveEvents(5) should be((CounterState.VALID, 100L, CounterState.VALID, 100L))
      listener.receiveEvents(6) should be((CounterState.VALID, 100L, CounterState.VALID, 100L))
      listener.receiveEvents(7) should be((CounterState.VALID, 100L, CounterState.VALID, 100L))
      listener.receiveEvents(8) should be((CounterState.VALID, 100L, CounterState.VALID, 100L))

      weakCounter.getValue should be(100L)

      listener.receiveEvents should have size 8

      handle.getCounterListener should be(listener)
      handle.remove()
    }
  }

  protected def withCacheManagers(configurationXml: String, numInstances: Int)(fun: Seq[EmbeddedCacheManager] => Unit): Unit = {
    val managers = (1 to numInstances).map(_ => new DefaultCacheManager(configurationXml))

    try {
      fun(managers)
    } finally {
      managers.foreach(_.stop())
    }
  }
}
