package org.littlewings.infinispan.spring

import java.util.concurrent.TimeUnit

import org.infinispan.configuration.cache.ConfigurationBuilder
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.spring.provider.SpringEmbeddedCacheManager
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.{Bean, Configuration}

@Configuration
class Config {
  @Bean
  def cacheManager: CacheManager = {
    val cacheConfiguration = new ConfigurationBuilder().expiration.lifespan(5L, TimeUnit.SECONDS).build
    val nativeCacheManager = new DefaultCacheManager
    nativeCacheManager.defineConfiguration("calcCache", cacheConfiguration)

    new SpringEmbeddedCacheManager(nativeCacheManager)
  }
}
