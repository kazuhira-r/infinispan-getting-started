package org.littlewings.infinispan.service

import javax.cache.annotation.{CacheKey, CachePut, CacheResult, CacheValue}
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class MessageService {
  @CachePut(cacheName = "transactionalCache")
  def putCache(@CacheKey key: String, @CacheValue message: String): Unit = ()

  @CacheResult(cacheName = "transactionalCache")
  def message(key: String): String = "default-message"
}
