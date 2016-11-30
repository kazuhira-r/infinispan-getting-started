package org.littlewings.infinispan.spring

import org.infinispan.client.hotrod.RemoteCacheManager
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder
import org.infinispan.spring.provider.SpringRemoteCacheManager
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.{Bean, Configuration}

@Configuration
class Config {
  @Bean
  def cacheManager: CacheManager = {
    val nativeCacheManager =
      new RemoteCacheManager(new ConfigurationBuilder().addServer.host("localhost").port(11222).build)
    new SpringRemoteCacheManager(nativeCacheManager)
  }
}
