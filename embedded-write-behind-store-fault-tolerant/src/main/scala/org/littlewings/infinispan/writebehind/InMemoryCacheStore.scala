package org.littlewings.infinispan.writebehind

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger, AtomicReference}
import java.util.concurrent.{ConcurrentHashMap, Executor}

import org.infinispan.commons.marshall.StreamingMarshaller
import org.infinispan.marshall.core.{MarshalledEntry, MarshalledEntryFactory}
import org.infinispan.metadata.InternalMetadata
import org.infinispan.persistence.spi.{AdvancedCacheWriter, AdvancedLoadWriteStore, InitializationContext}
import org.infinispan.util.TimeService
import org.jboss.logging.Logger

import scala.collection.JavaConverters._

object InMemoryCacheStore {
  val COUNTER: AtomicInteger = new AtomicInteger(0)
  val AVAILABLE: AtomicBoolean = new AtomicBoolean(true)

  private val CURRENT_STORE: AtomicReference[InMemoryCacheStore[_, _]] = new AtomicReference[InMemoryCacheStore[_, _]]

  def currentStoreEntries[K, V]: Map[K, V] =
    CURRENT_STORE.get.underlyingStore.map { case (k, (v, _)) => (k.asInstanceOf[K], v.asInstanceOf[V]) }.toMap
}

class InMemoryCacheStore[K, V] extends AdvancedLoadWriteStore[K, V] {
  val logger: Logger = Logger.getLogger(getClass)

  var configuration: InMemoryCacheStoreConfiguration = _
  var marshaller: StreamingMarshaller = _
  var marshalledEntryFactory: MarshalledEntryFactory[K, V] = _
  var timeService: TimeService = _

  val underlyingStore: scala.collection.mutable.Map[K, (V, InternalMetadata)] =
    new ConcurrentHashMap[K, (V, InternalMetadata)]().asScala

  val failedKeyCounter: scala.collection.mutable.Map[K, Int] =
    new ConcurrentHashMap[K, Int]().asScala

  override def start(): Unit = {
    logger.infof("InMemoryCacheLoadWriteStore started")

    InMemoryCacheStore.CURRENT_STORE.set(this)
  }

  override def stop(): Unit = {
    logger.infof("InMemoryCacheLoadWriteStore stopped")
  }

  override def init(ctx: InitializationContext): Unit = {
    configuration = ctx.getConfiguration.asInstanceOf[InMemoryCacheStoreConfiguration]
    marshaller = ctx.getMarshaller
    marshalledEntryFactory = ctx.getMarshalledEntryFactory.asInstanceOf[MarshalledEntryFactory[K, V]]
    timeService = ctx.getTimeService

    logger.infof("InMemoryCacheLoadWriteStore initialized")
  }

  override def size(): Int = underlyingStore.size

  override def clear(): Unit = underlyingStore.clear()

  override def purge(threadPool: Executor, listener: AdvancedCacheWriter.PurgeListener[_ >: K]): Unit = {
    logger.infof("purge started")

    val now = timeService.wallClockTime()

    underlyingStore
      .foreach { case (k, (_, meta)) =>
        if (meta.isExpired(now)) {
          logger.infof("purge key = %s", k)
          listener.entryPurged(k)
        }
      }

    logger.infof("purge end")
  }

  override def isAvailable: Boolean = InMemoryCacheStore.AVAILABLE.get

  override def write(entry: MarshalledEntry[_ <: K, _ <: V]): Unit = {
    val key = entry.getKey
    val value = entry.getValue

    failedKeyCounter
      .get(key)
      .orElse(Option(0))
      .filter(_ < InMemoryCacheStore.COUNTER.get)
      .foreach { v =>
        failedKeyCounter.put(key, v + 1)
        logger.infof("Oops!! key = %s, failed-count = %d", key, v)
        throw new RuntimeException(s"Oops!! key = ${key} failed-count = ${v}")
      }

    failedKeyCounter.put(key, 0)

    logger.infof("write key = %s, value = %s", key, value)

    underlyingStore.put(key, (value, entry.getMetadata))
  }

  override def delete(key: scala.Any): Boolean = {
    logger.infof("delete key = %s", key)
    underlyingStore.remove(key.asInstanceOf[K]).isDefined
  }

  override def load(key: scala.Any): MarshalledEntry[K, V] = {
    val loaded = underlyingStore
      .get(key.asInstanceOf[K])
      .map { case (value, metadata) => marshalledEntryFactory.newMarshalledEntry(key, value, metadata) }
      .orNull

    logger.infof("loaded key = %s, value = %s", key, loaded)

    loaded
  }

  override def contains(key: scala.Any): Boolean = underlyingStore.contains(key.asInstanceOf[K])
}
