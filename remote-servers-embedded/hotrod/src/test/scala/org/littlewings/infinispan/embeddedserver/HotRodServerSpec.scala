package org.littlewings.infinispan.embeddedserver

import org.infinispan.client.hotrod.RemoteCacheManager
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder
import org.infinispan.commons.equivalence.ByteArrayEquivalence
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.server.hotrod.HotRodServer
import org.infinispan.server.hotrod.configuration.HotRodServerConfigurationBuilder
import org.scalatest.{FunSpec, Matchers}

class HotRodServerSpec extends FunSpec with Matchers {
  describe("Hot Rod Embedded Server") {
    it("getting started") {
      // Embedded Cache setup
      val embeddedCacheManager = new DefaultCacheManager
      embeddedCacheManager
        .defineConfiguration(
          "hotRodCache",
          new org.infinispan.configuration.cache.ConfigurationBuilder()
            .dataContainer()
            .keyEquivalence(ByteArrayEquivalence.INSTANCE)
            .valueEquivalence(ByteArrayEquivalence.INSTANCE)
            .build
        )

      // Hot Rod Server setup
      val hotRodServerHost = "localhost"
      val hotRodServerPort = 11222
      val hotRodServer = new HotRodServer
      hotRodServer.start(
        new HotRodServerConfigurationBuilder()
          .host(hotRodServerHost)
          .port(hotRodServerPort)
          .workerThreads(Runtime.getRuntime.availableProcessors) // デフォルト値
          .build,
        embeddedCacheManager
      )

      // Hot Rod Client setup
      val remoteCacheManager = new RemoteCacheManager(
        new ConfigurationBuilder().addServers(s"${hotRodServerHost}:${hotRodServerPort}").build
      )
      val remoteCache = remoteCacheManager.getCache[String, Book]("hotRodCache")

      // use Cache
      val book = Book("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 5337)

      remoteCache.put(book.isbn, book)

      remoteCache should have size (1)
      remoteCache.get(book.isbn).title should be("Infinispan Data Grid Platform Definitive Guide")
      remoteCache.get(book.isbn).price should be(5337)

      // resource clean up
      remoteCache.stop()
      remoteCacheManager.stop()
      hotRodServer.stop
      embeddedCacheManager.stop()
    }
  }
}
