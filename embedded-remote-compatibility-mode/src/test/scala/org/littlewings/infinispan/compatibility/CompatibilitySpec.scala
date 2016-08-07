package org.littlewings.infinispan.compatibility

import org.infinispan.Cache
import org.infinispan.client.hotrod.RemoteCacheManager
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.server.hotrod.HotRodServer
import org.infinispan.server.hotrod.configuration.HotRodServerConfigurationBuilder
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class CompatibilitySpec extends FunSpec {
  describe("Compatibility Spec") {
    it("disabled compatibility mode, simple") {
      withEmbeddedCache[String, String](2, "noCompatibilityCache") { embeddedCache =>
        val embeddedCacheManager = embeddedCache.getCacheManager

        val remoteServerPort = 11222
        val remoteServer = new HotRodServer
        remoteServer.start(
          new HotRodServerConfigurationBuilder().host("localhost").port(remoteServerPort).build,
          embeddedCacheManager
        )

        val remoteCacheManager =
          new RemoteCacheManager(
            new ConfigurationBuilder().addServers(s"localhost:$remoteServerPort").build
          )
        val remoteCache = remoteCacheManager.getCache[String, String]("noCompatibilityCache")

        embeddedCache.put("key-from-embedded", "value-from-embedded")
        remoteCache.get("key-from-embedded") should be(null)
        embeddedCache.get("key-from-embedded") should be("value-from-embedded")

        remoteCache.put("key-from-remote", "value-from-remote")
        embeddedCache.get("key-from-remote") should be(null)
        remoteCache.get("key-from-remote") should be(null)

        remoteCache.stop()
        remoteServer.stop
      }
    }

    it("enabled compatibility mode, simple") {
      withEmbeddedCache[String, String](2, "compatibilityCache") { embeddedCache =>
        val embeddedCacheManager = embeddedCache.getCacheManager

        val remoteServerPort = 11222
        val remoteServer = new HotRodServer
        remoteServer.start(
          new HotRodServerConfigurationBuilder().host("localhost").port(remoteServerPort).build,
          embeddedCacheManager
        )

        val remoteCacheManager =
          new RemoteCacheManager(
            new ConfigurationBuilder().addServers(s"localhost:$remoteServerPort").build
          )
        val remoteCache = remoteCacheManager.getCache[String, String]("compatibilityCache")

        embeddedCache.put("key-from-embedded", "value-from-embedded")
        remoteCache.get("key-from-embedded") should be("value-from-embedded")
        embeddedCache.get("key-from-embedded") should be("value-from-embedded")

        remoteCache.put("key-from-remote", "value-from-remote")
        embeddedCache.get("key-from-remote") should be("value-from-remote")
        remoteCache.get("key-from-remote") should be("value-from-remote")

        remoteCache.stop()
        remoteServer.stop
      }
    }

    it("enabled compatibility mode, use case class") {
      withEmbeddedCache[String, Person](2, "compatibilityCache") { embeddedCache =>
        val embeddedCacheManager = embeddedCache.getCacheManager

        val remoteServerPort = 11222
        val remoteServer = new HotRodServer
        remoteServer.start(
          new HotRodServerConfigurationBuilder().host("localhost").port(remoteServerPort).build,
          embeddedCacheManager
        )

        val remoteCacheManager =
          new RemoteCacheManager(
            new ConfigurationBuilder().addServers(s"localhost:$remoteServerPort").build
          )
        val remoteCache = remoteCacheManager.getCache[String, Person]("compatibilityCache")

        embeddedCache.put("key-from-embedded", new Person("磯野カツオ", 11))
        remoteCache.get("key-from-embedded") should be(new Person("磯野カツオ", 11))
        embeddedCache.get("key-from-embedded") should be(new Person("磯野カツオ", 11))

        remoteCache.put("key-from-remote", new Person("磯野ワカメ", 9))
        embeddedCache.get("key-from-remote") should be(new Person("磯野ワカメ", 9))
        remoteCache.get("key-from-remote") should be(new Person("磯野ワカメ", 9))

        remoteCache.stop()
        remoteServer.stop
      }
    }
  }

  protected def withEmbeddedCache[K, V](numInstances: Int, cacheName: String)(f: Cache[K, V] => Unit): Unit = {
    val managers = (1 to numInstances).map(_ => new DefaultCacheManager("infinispan.xml"))
    managers.foreach(_.getCache[K, V](cacheName))

    try {
      val cache = managers.head.getCache[K, V](cacheName)

      f(cache)

      cache.stop()
    } finally {
      managers.foreach(_.stop())
    }
  }
}
