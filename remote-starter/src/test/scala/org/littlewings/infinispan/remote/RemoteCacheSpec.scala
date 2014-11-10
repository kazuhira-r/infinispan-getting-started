package org.littlewings.infinspan.remote

import java.util.concurrent.TimeUnit

// for Hot Rod Client
import org.infinispan.client.hotrod.RemoteCacheManager
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder

// for Memcached Client
import java.net.InetSocketAddress
import net.spy.memcached.MemcachedClient

// for REST Client
import dispatch._
import dispatch.Defaults._
import org.json4s._
import org.json4s.jackson.Serialization

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class RemoteCacheSpec extends FunSpec {
  describe("Infinispan RemoteCache Spec") {
    it("Hot Rod Client") {
      val manager =
        new RemoteCacheManager(new ConfigurationBuilder()
          .addServer
          .host("localhost")
          .port(11222)
          .build)

      val cache = manager.getCache[String, String]("hotrodCache")

      cache.put("key1", "value1")
      cache.put("key2", "value2")

      cache.get("key1") should be ("value1")
      cache.get("key2") should be ("value2")
      cache.get("non-exists-key") should be (null)

      cache.remove("key1")
      cache.remove("key2")

      cache.get("key1") should be (null)
      cache.get("key2") should be (null)

      cache.stop()
      manager.stop()
    }

    it("Hot Rod Client, using non exists Cache") {
      val manager =
        new RemoteCacheManager(new ConfigurationBuilder()
          .addServer
          .host("localhost")
          .port(11222)
          .build)

      manager.getCache[String, String]("nonExistsCache") should be (null)

      manager.stop()
    }

    it("Hot Rod Client, with expiration") {
      val manager =
        new RemoteCacheManager(new ConfigurationBuilder()
        .addServer
        .host("localhost")
        .port(11222)
        .build)

      val cache = manager.getCache[String, String]("hotrodWithExpiredCache")

      cache.put("key1", "value1")
      cache.put("key2", "value2")

      TimeUnit.SECONDS.sleep(2)

      cache.get("key1") should be ("value1")

      TimeUnit.SECONDS.sleep(2)

      // maxIdle
      cache.get("key1") should be ("value1")
      cache.get("key2") should be (null)

      TimeUnit.SECONDS.sleep(2)

      // lifespan
      cache.get("key1") should be (null)
      cache.get("key2") should be (null)

      cache.stop()
      manager.stop()
    }

    it("Memcached Client") {
      val memcachedClient = new MemcachedClient(new InetSocketAddress("localhost", 11211))

      memcachedClient.set("key1", 3, "value1")  // expire 3 sec
      memcachedClient.set("key2", 3, "value2")  // expire 3sec

      memcachedClient.get("key1") should be ("value1")
      memcachedClient.get("key2") should be ("value2")

      TimeUnit.SECONDS.sleep(3)

      memcachedClient.get("key1") should be (null)
      memcachedClient.get("key2") should be (null)
    }

    it("REST Client") {
      val http = Http()

      val baseUrl = host("localhost", 8080) / "rest" / "restCache"
      val baseReq =
        baseUrl
          .setContentType("application/json", "UTF-8")

      val p1 = Pair("key1", "value1")
      val p2 = Pair("key2", "value2")

      implicit val formats = Serialization.formats(NoTypeHints)

      // PUT
      val p1RegisterReq = (baseReq / p1.key).PUT << Serialization.write(p1)
      http(p1RegisterReq).apply().getStatusCode should be (200)

      val p2RegisterReq = (baseReq / p2.key).PUT << Serialization.write(p2)
      http(p2RegisterReq).apply().getStatusCode should be (200)

      // GET
      val p1GetReq = baseReq / p1.key
      val p1GetRes = http(p1GetReq OK as.json4s.Json)
      p1GetRes.apply().extract[Pair] should be (p1)

      val p2GetReq = baseReq / p2.key
      val p2GetRes = http(p2GetReq OK as.json4s.Json)
      p2GetRes.apply().extract[Pair] should be (p2)

      // DELETE
      val p1DeleteReq = (baseReq / p1.key).DELETE << Serialization.write(p1)
      http(p1DeleteReq).apply().getStatusCode should be (200)

      val p2DeleteReq = (baseReq / p2.key).DELETE << Serialization.write(p2)
      http(p2DeleteReq).apply().getStatusCode should be (200)

      // GET
      http(p1GetReq).apply().getStatusCode should be (404)
      http(p2GetReq).apply().getStatusCode should be (404)

      http.shutdown()
    }
  }
}

case class Pair(key: String, value: String)
