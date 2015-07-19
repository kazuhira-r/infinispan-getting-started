package org.littlewings.infinispan.producer

import javax.enterprise.context.{ApplicationScoped, Dependent}
import javax.enterprise.inject.{Disposes, Produces}

import org.infinispan.manager.{DefaultCacheManager, EmbeddedCacheManager}

@Dependent
class CacheProducer {
  @ApplicationScoped
  @Produces
  def createEmbeddedCacheManager: EmbeddedCacheManager = new DefaultCacheManager("infinispan.xml")

  def destroyEmbeddedCacheManager(@Disposes embeddedCacheManager: EmbeddedCacheManager): Unit = embeddedCacheManager.stop()
}
