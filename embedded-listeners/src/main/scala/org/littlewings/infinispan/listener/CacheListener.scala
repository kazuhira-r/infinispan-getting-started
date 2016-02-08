package org.littlewings.infinispan.listener

import org.infinispan.notifications.Listener
import org.infinispan.notifications.cachelistener.annotation.{CacheEntriesEvicted, CacheEntryCreated, CacheEntryModified, CacheEntryRemoved}
import org.infinispan.notifications.cachelistener.event.{CacheEntriesEvictedEvent, CacheEntryEvent}

trait CacheListener[K, V] {
  val name: String

  @CacheEntryCreated
  @CacheEntryModified
  @CacheEntryRemoved
  def handleEvent(event: CacheEntryEvent[K, V]): Unit = {
    println(s"[${getClass.getSimpleName}]:${name} event = ${event.getType}, isPre = ${event.isPre}, key = ${event.getKey}, value = ${event.getValue}")
  }

  @CacheEntriesEvicted
  def handleEvictedEvent(event: CacheEntriesEvictedEvent[K, V]): Unit = {
    println(s"[${getClass.getSimpleName}]:${name} event = ${event.getType}, isPre = ${event.isPre}, entries = ${event.getEntries}")
  }
}

@Listener
class LocalListener[K, V](val name: String) extends CacheListener[K, V]

@Listener(clustered = true)
class ClusteredListener[K, V](val name: String) extends CacheListener[K, V]
