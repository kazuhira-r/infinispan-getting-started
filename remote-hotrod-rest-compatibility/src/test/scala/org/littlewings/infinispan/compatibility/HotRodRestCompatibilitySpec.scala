package org.littlewings.infinispan.compatibility

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}
import javax.ws.rs.client.{ClientBuilder, Entity}
import javax.ws.rs.core.Response

import org.infinispan.client.hotrod.RemoteCacheManager
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder
import org.infinispan.client.hotrod.exceptions.HotRodClientException
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class HotRodRestCompatibilitySpec extends FunSpec {
  describe("Hot Rod & REST Compatibility Spec") {
    it("simple cache, compatibility disabled") {
      val manager = new RemoteCacheManager(new ConfigurationBuilder().addServers("localhost:11222").build)
      val cache = manager.getCache[String, Any]("namedCache")

      val client = ClientBuilder.newBuilder.build

      try {
        cache.put("key1", "value1")
        cache.get("key1") should be("value1")

        val getResponse =
          client
            .target("http://localhost:8080/rest/namedCache/key1")
            .request
            .get

        getResponse.getStatus should be(Response.Status.NOT_FOUND.getStatusCode)
        getResponse.readEntity(classOf[String]) should be("")
        getResponse.close()

        val putResponse =
          client
            .target("http://localhost:8080/rest/namedCache/key2")
            .request
            .put(Entity.text("value2"))

        putResponse.getStatus should be(Response.Status.OK.getStatusCode)
        putResponse.close()

        cache.get("key2").asInstanceOf[String] should be(null)
      } finally {
        cache.clear()
        cache.stop()
        manager.stop()

        client.close()
      }
    }

    it("simple cache, compatibility enabled") {
      val manager = new RemoteCacheManager(new ConfigurationBuilder().addServers("localhost:11222").build)
      val cache = manager.getCache[String, Any]("compatibilityCache")

      val client = ClientBuilder.newBuilder.build

      try {
        cache.put("key1", "value1")
        cache.get("key1") should be("value1")

        val getResponse =
          client
            .target("http://localhost:8080/rest/compatibilityCache/key1")
            .request
            .get

        getResponse.getStatus should be(Response.Status.OK.getStatusCode)
        getResponse.readEntity(classOf[String]) should be("value1")
        getResponse.close()

        val putResponse =
          client
            .target("http://localhost:8080/rest/compatibilityCache/key2")
            .request
            .put(Entity.text("value2"))

        putResponse.getStatus should be(Response.Status.OK.getStatusCode)
        putResponse.close()

        new String(cache.get("key2").asInstanceOf[Array[Byte]]) should be("value2")
      } finally {
        cache.clear()
        cache.stop()
        manager.stop()

        client.close()
      }
    }

    it("simple cache, compatibility enabled, as Java Serialized Object") {
      val manager = new RemoteCacheManager(new ConfigurationBuilder().addServers("localhost:11222").build)
      val cache = manager.getCache[String, String]("compatibilityCache")

      val client = ClientBuilder.newBuilder.build

      try {
        cache.put("key1", "value1")
        cache.get("key1") should be("value1")

        val getResponse =
          client
            .target("http://localhost:8080/rest/compatibilityCache/key1")
            .request("application/x-java-serialized-object")
            .get

        getResponse.getStatus should be(Response.Status.OK.getStatusCode)
        getResponse.readEntity(classOf[String]) should be("value1")
        getResponse.close()

        val putResponse =
          client
            .target("http://localhost:8080/rest/compatibilityCache/key2")
            .request("application/x-java-serialized-object")
            .put(Entity.entity(javaSerialize("value2"), "application/x-java-serialized-object"))

        putResponse.getStatus should be(Response.Status.OK.getStatusCode)
        putResponse.close()

        cache.get("key2") should be("value2")
      } finally {
        cache.clear()
        cache.stop()
        manager.stop()

        client.close()
      }
    }

    it("user defined class cache, compatibility disabled") {
      val manager = new RemoteCacheManager(new ConfigurationBuilder().addServers("localhost:11222").build)
      val cache = manager.getCache[String, Person]("namedCache")

      val client = ClientBuilder.newBuilder.build

      try {
        cache.put("key1", new Person("カツオ", "磯野"))
        cache.get("key1") should be(new Person("カツオ", "磯野"))

        val getResponse =
          client
            .target("http://localhost:8080/rest/namedCache/key1")
            .request("application/x-java-serialized-object")
            .get

        getResponse.getStatus should be(Response.Status.NOT_FOUND.getStatusCode)
        getResponse.close()

        val putResponse =
          client
            .target("http://localhost:8080/rest/namedCache/key2")
            .request("application/x-java-serialized-object")
            .put(Entity.entity(javaSerialize(new Person("ワカメ", "磯野")), "application/x-java-serialized-object"))

        putResponse.getStatus should be(Response.Status.OK.getStatusCode)
        putResponse.close()

        cache.get("key2") should be(null)
      } finally {
        cache.clear()
        cache.stop()
        manager.stop()

        client.close()
      }
    }

    it("user defined class cache, compatibility enabled") {
      val manager = new RemoteCacheManager(new ConfigurationBuilder().addServers("localhost:11222").build)
      val cache = manager.getCache[String, Person]("compatibilityCache")

      val client = ClientBuilder.newBuilder.build

      try {
        // a[HotRodClientException] should be thrownBy cache.put("key1", new Person("カツオ", "磯野"))

        cache.put("key1", new Person("カツオ", "磯野"))
        cache.get("key1") should be(new Person("カツオ", "磯野"))

        val getResponse =
          client
            .target("http://localhost:8080/rest/compatibilityCache/key1")
            .request("application/x-java-serialized-object")
            .get

        getResponse.getStatus should be(Response.Status.OK.getStatusCode)
        javaDeserialize[Person](getResponse.readEntity(classOf[Array[Byte]])) should be(new Person("カツオ", "磯野"))
        getResponse.close()

        val putResponse =
          client
            .target("http://localhost:8080/rest/compatibilityCache/key2")
            .request("application/x-java-serialized-object")
            .put(Entity.entity(javaSerialize(new Person("ワカメ", "磯野")), "application/x-java-serialized-object"))

        putResponse.getStatus should be(Response.Status.OK.getStatusCode)
        putResponse.close()

        cache.get("key2") should be(new Person("ワカメ", "磯野"))
      } finally {
        cache.clear()
        cache.stop()
        manager.stop()

        client.close()
      }
    }
  }

  private def javaDeserialize[T](binary: Array[Byte]): T =
    new ObjectInputStream(new ByteArrayInputStream(binary)).readObject.asInstanceOf[T]

  private def javaSerialize(target: Any): Array[Byte] = {
    val baos = new ByteArrayOutputStream
    val oos = new ObjectOutputStream(baos)
    oos.writeObject(target)
    oos.flush()
    baos.toByteArray
  }
}
