package org.littlewings.infinispan.scripting

import java.util.Collections

import org.infinispan.client.hotrod.{RemoteCacheManager, RemoteCache}
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder
import org.scalatest.{FunSpec, Matchers}

import scala.io.Source

class ScriptingSpec extends FunSpec with Matchers {
  describe("Scripting Spec") {
    it("simple multiply scripting, using literal") {
      withRemoteCache[Int, Int]("namedCache") { cache =>
        val manager = cache.getRemoteCacheManager
        val scriptCache = manager.getCache[String, String]("___script_cache")

        scriptCache.put("simple.js", "a * b")

        val params = new java.util.HashMap[String, Int]
        params.put("a", 5)
        params.put("b", 6)

        val result = cache.execute[Double]("simple.js", params)

        result should be(30)
      }
    }

    it("using script, defined variables") {
      withRemoteCache[Int, Int]("namedCache") { cache =>
        val manager = cache.getRemoteCacheManager
        val scriptCache = manager.getCache[String, String]("___script_cache")

        scriptCache.put("noBindings.js", readScript("scripts/noBindings.js"))

        val result = cache.execute[String]("noBindings.js", Collections.emptyMap[String, String])

        result should be("Hello Scripting!!")
      }
    }

    it("using script, pass variables") {
      withRemoteCache[String, Int]("namedCache") { cache =>
        val manager = cache.getRemoteCacheManager
        val scriptCache = manager.getCache[String, String]("___script_cache")

        scriptCache.put("bindings.js", readScript("scripts/bindings.js"))

        (1 to 10).foreach(i => cache.put(s"$key$i", i))

        val params = new java.util.HashMap[String, Int]
        params.put("a", 5)
        params.put("b", 8)

        val result = cache.execute[Int]("bindings.js", params)

        result should be(55)
      }
    }

    it("using script, pass variables as local") {
      withRemoteCache[String, Int]("namedCache") { cache =>
        val manager = cache.getRemoteCacheManager
        val scriptCache = manager.getCache[String, String]("___script_cache")

        scriptCache.put("localBindings.js", readScript("scripts/localBindings.js"))

        (1 to 10).foreach(i => cache.put(s"$key$i", i))

        val params = new java.util.HashMap[String, Int]
        params.put("a", 5)
        params.put("b", 8)

        val result = cache.execute[Int]("localBindings.js", params)

        result should be(55)
      }
    }

    it("using script in compatibility mode, pass variables as local") {
      withRemoteCache[String, Int]("compatibilityCache") { cache =>
        val manager = cache.getRemoteCacheManager
        val scriptCache = manager.getCache[String, String]("___script_cache")

        scriptCache.put("localCompatibilityBindings.js", readScript("scripts/localCompatibilityBindings.js"))

        (1 to 10).foreach(i => cache.put(s"$key$i", i))

        val params = new java.util.HashMap[String, Int]
        params.put("a", 5)
        params.put("b", 8)

        val result = cache.execute[Int]("localCompatibilityBindings.js", params)

        result should be(55)
      }
    }

    it("using script, as distributed") {
      withRemoteCache[String, Int]("namedCache") { cache =>
        val manager = cache.getRemoteCacheManager
        val scriptCache = manager.getCache[String, String]("___script_cache")

        scriptCache.put("distributedBindings.js", readScript("scripts/distributedBindings.js"))

        (1 to 10).foreach(i => cache.put(s"$key$i", i))

        // distributedの場合は、パラメーターを渡しても無視される…
        val result = cache.execute[java.util.List[Int]]("distributedBindings.js", Collections.emptyMap[String, String])

        result should contain theSameElementsAs List(8, 8)
      }
    }

    it("using script in compatibility mode, as distributed") {
      withRemoteCache[String, Int]("compatibilityCache") { cache =>
        val manager = cache.getRemoteCacheManager
        val scriptCache = manager.getCache[String, String]("___script_cache")

        scriptCache.put("distributedCompatibilityBindings.js", readScript("scripts/distributedCompatibilityBindings.js"))

        (1 to 10).foreach(i => cache.put(s"$key$i", i))

        val result = cache.execute[java.util.List[Int]]("distributedCompatibilityBindings.js", Collections.emptyMap[String, String])

        result should contain theSameElementsAs List(8, 8)
      }
    }
  }

  protected def readScript(path: String): String = {
    val is = Thread.currentThread.getContextClassLoader.getResourceAsStream(path)

    val source = Source.fromInputStream(is, "UTF-8")
    try {
      source.mkString
    } finally {
      source.close()
    }
  }

  protected def withRemoteCache[K, V](cacheName: String)(fun: RemoteCache[K, V] => Unit): Unit = {
    val manager =
      new RemoteCacheManager(new ConfigurationBuilder().addServer().host("localhost").port(11222).build())

    try {
      val cache = manager.getCache[K, V](cacheName)
      fun(cache)
      cache.clear()
      cache.stop()
    } finally {
      manager.stop()
    }
  }
}
