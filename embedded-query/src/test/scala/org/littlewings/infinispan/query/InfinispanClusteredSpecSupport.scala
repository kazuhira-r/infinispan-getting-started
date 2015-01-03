package org.littlewings.infinispan.query

import org.infinispan.Cache
import org.infinispan.manager.DefaultCacheManager

trait InfinispanClusteredSpecSupport {
  protected def withCache[K, V](numInstances: Int, fileName: String, cacheName: String)(fun: Cache[K, V] => Unit): Unit = {
    val managers = (1 to numInstances).map(_ => new DefaultCacheManager(fileName))

    try {
      managers.foreach(_.getCache[K, V](cacheName))

      val manager = managers.head
      val cache = manager.getCache[K, V](cacheName)

      fun(cache)
    } finally {
      managers.foreach(_.stop())
    }
  }
}
