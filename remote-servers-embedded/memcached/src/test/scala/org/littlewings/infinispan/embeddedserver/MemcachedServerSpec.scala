package org.littlewings.infinispan.embeddedserver

import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

import net.spy.memcached.MemcachedClient
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.server.memcached.MemcachedServer
import org.infinispan.server.memcached.configuration.MemcachedServerConfigurationBuilder
import org.scalatest.{FunSpec, Matchers}

class MemcachedServerSpec extends FunSpec with Matchers {
  describe("Memcached Embedded Server") {
    it("getting started") {
      // Embeddd Cache setup
      val embeddecCacheManager = new DefaultCacheManager

      // Memcached Server setup
      val memcachedHost = "localhost"
      val memcachedPort = 11211
      val memcachedServer = new MemcachedServer
      memcachedServer.start(
        new MemcachedServerConfigurationBuilder()
          .defaultCacheName("memcachedCache")
          .host(memcachedHost)
          .port(memcachedPort)
          .workerThreads(Runtime.getRuntime.availableProcessors) // デフォルト値
          .build,
        embeddecCacheManager
      )

      // Memcached Client setup
      val client = new MemcachedClient(new InetSocketAddress(memcachedHost, memcachedPort))

      // use Cache
      val book = Book("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 5337)

      client.set(book.isbn, 3, book)
      client.get(book.isbn).asInstanceOf[Book].title should be("Infinispan Data Grid Platform Definitive Guide")
      client.get(book.isbn).asInstanceOf[Book].price should be(5337)

      TimeUnit.SECONDS.sleep(5L)

      client.get(book.isbn) should be(null)

      // resource clean up
      client.shutdown()
      memcachedServer.stop
      embeddecCacheManager.stop()
    }
  }
}
