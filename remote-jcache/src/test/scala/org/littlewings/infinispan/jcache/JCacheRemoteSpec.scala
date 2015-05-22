package org.littlewings.infinispan.jcache

import java.util
import java.util.Properties
import java.util.concurrent.TimeUnit
import javax.cache.configuration.{Configuration, MutableConfiguration}
import javax.cache.expiry.{AccessedExpiryPolicy, Duration}
import javax.cache.processor.EntryProcessorResult
import javax.cache.{CacheException, Cache, Caching}

import scala.collection.JavaConverters._

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class JCacheRemoteSpec extends FunSpec {
  describe("Infinispan JCache Remote Spec") {
    it("simple usage") {
      val provider = Caching.getCachingProvider
      val manager = provider.getCacheManager

      val configuration =
        new MutableConfiguration[String, String]()
          .setTypes(classOf[String], classOf[String])

      val cache: Cache[String, String] = manager.createCache("namedCache", configuration)

      cache.put("key1", "value1")
      cache.get("key1") should be("value1")

      cache.remove("key1")

      cache.close()
      manager.close()
      provider.close()
    }

    it("with expiry") {
      val provider = Caching.getCachingProvider
      val manager = provider.getCacheManager

      val configuration =
        new MutableConfiguration()
          .setTypes(classOf[String], classOf[String])
          .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS, 5)))

      val cache: Cache[String, String] = manager.createCache("namedCache", configuration)

      cache.put("key1", "value1")
      cache.put("key2", "value2")

      TimeUnit.SECONDS.sleep(3L)

      cache.get("key1")

      TimeUnit.SECONDS.sleep(3L)

      cache.get("key1") should be("value1")
      cache.get("key2") should be(null)

      cache.close()
      manager.close()
      provider.close()
    }

    it("predefined?") {
      val provider = Caching.getCachingProvider
      val manager = provider.getCacheManager

      manager.getCache("namedCache", classOf[String], classOf[String]) should be(null)

      manager.close()
      provider.close()
    }

    it("not found cache") {
      val provider = Caching.getCachingProvider
      val manager = provider.getCacheManager

      val configuration =
        new MutableConfiguration[String, String]()
          .setTypes(classOf[String], classOf[String])

      a[CacheException] should be thrownBy {
        manager.createCache[String, String, Configuration[String, String]]("testCache", configuration)
      }

      manager.close()
      provider.close()
    }

    it("with Properties") {
      val properties = new Properties
      properties.setProperty("infinispan.client.hotrod.server_list", "localhost:11222")
      // 以下でも可
      // org.infinispan.client.hotrod.impl.ConfigurationProperties に定義
      //properties.setProperty(ConfigurationProperties.SERVER_LIST, "localhost:11222")
      properties.setProperty("infinispan.client.hotrod.protocol_version", "2.1")


      val provider = Caching.getCachingProvider
      val manager = provider.getCacheManager(provider.getDefaultURI, provider.getDefaultClassLoader, properties)

      val configuration =
        new MutableConfiguration[String, String]()
          .setTypes(classOf[String], classOf[String])

      val cache: Cache[String, String] = manager.createCache("namedCache", configuration)

      cache.put("key1", "value1")
      cache.get("key1") should be("value1")

      cache.remove("key1")

      cache.close()
      manager.close()
      provider.close()
    }

    it("user defined class") {
      val provider = Caching.getCachingProvider
      val manager = provider.getCacheManager

      val configuration =
        new MutableConfiguration[String, Person]()
          .setTypes(classOf[String], classOf[Person])

      val cache: Cache[String, Person] = manager.createCache("namedCache", configuration)

      cache.put("1", new Person("カツオ", "磯野", 12))
      cache.get("1").firstName should be("カツオ")

      cache.remove("key1")

      cache.close()
      manager.close()
      provider.close()
    }

    it("entry processor") {
      val provider = Caching.getCachingProvider
      val manager = provider.getCacheManager

      val configuration =
        new MutableConfiguration[String, Integer]()
          .setTypes(classOf[String], classOf[Integer])

      val cache: Cache[String, Integer] = manager.createCache("namedCache", configuration)

      (1 to 10).foreach(i => cache.put(s"key$i", i))

      val keys = (1 to 10).map(i => s"key$i").toSet.asJava
      val result: util.Map[String, EntryProcessorResult[Integer]] = cache.invokeAll(keys, new DoublingEntryProcessor)

      result.asScala.values.map(_.get).foldLeft(0)(_ + _) should be(110)

      cache.clear()

      cache.close()
      manager.close()
      provider.close()
    }

    it("entry processor, with user defined class") {
      val provider = Caching.getCachingProvider
      val manager = provider.getCacheManager

      val configuration =
        new MutableConfiguration[String, Person]()
          .setTypes(classOf[String], classOf[Person])

      val cache: Cache[String, Person] = manager.createCache("namedCache", configuration)

      cache.put("1", new Person("カツオ", "磯野", 12))

      cache.invoke("1", new FirstNameEntryProcessor) should be("カツオ")

      cache.clear()

      cache.close()
      manager.close()
      provider.close()
    }
  }
}

@SerialVersionUID(1L)
class Person(val firstName: String, val lastName: String, val age: Int) extends Serializable
