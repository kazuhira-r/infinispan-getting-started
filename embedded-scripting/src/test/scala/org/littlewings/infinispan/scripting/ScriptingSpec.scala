package org.littlewings.infinispan.scripting

import java.util
import java.util.concurrent.ExecutionException
import javax.script.SimpleBindings

import org.infinispan.scripting.ScriptingManager
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class ScriptingSpec extends FunSpec with InfinispanSpecSupport {
  describe("Infinispan Scripting Spec") {
    describe("with local-cache spec") {
      it("no Cache binding") {
        withCache[String, Integer]("localCache") { cache =>
          (1 to 5).foreach(i => cache.put(s"key$i", i))

          val scriptingManager =
            cache.getCacheManager.getGlobalComponentRegistry.getComponent(classOf[ScriptingManager])

          scriptingManager.addScript("noCacheBindings.js", loadScript("scripts/noCacheBindings.js"))

          val future = scriptingManager.runScript[String]("noCacheBindings.js")
          future.get should be("Hello Scripting!!")
        }
      }

      it("no Cache binding, with user parameters") {
        withCache[String, Integer]("localCache") { cache =>
          (1 to 5).foreach(i => cache.put(s"key$i", i))

          val scriptingManager =
            cache.getCacheManager.getGlobalComponentRegistry.getComponent(classOf[ScriptingManager])

          scriptingManager.addScript("noCacheWithBindings.js", loadScript("scripts/noCacheWithBindings.js"))

          val bindings = new SimpleBindings
          bindings.put("cacheName", "localCache")

          val future = scriptingManager.runScript[util.List[_]]("noCacheWithBindings.js", bindings)
          future.get should be(util.Arrays.asList(9, "org.infinispan.scripting.impl.ScriptingManagerImpl"))
        }
      }

      it("with Cache binding") {
        withCache[String, Integer]("localCache") { cache =>
          (1 to 5).foreach(i => cache.put(s"key$i", i))

          val scriptingManager =
            cache.getCacheManager.getGlobalComponentRegistry.getComponent(classOf[ScriptingManager])

          scriptingManager.addScript("withCacheBingings.js", loadScript("scripts/withCacheBingings.js"))

          val future = scriptingManager.runScript[Integer]("withCacheBingings.js", cache)
          future.get should be(9)
        }
      }

      it("with Cache binding, with user parameters") {
        withCache[String, Integer]("localCache") { cache =>
          (1 to 5).foreach(i => cache.put(s"key$i", i))

          val scriptingManager =
            cache.getCacheManager.getGlobalComponentRegistry.getComponent(classOf[ScriptingManager])

          scriptingManager.addScript("withCacheWithParameters.js", loadScript("scripts/withCacheWithParameters.js"))

          val bindings = new SimpleBindings
          bindings.put("prefix", "★")
          bindings.put("suffix", "★")

          val future = scriptingManager.runScript[Integer]("withCacheWithParameters.js", cache, bindings)
          future.get should be("★localCache★")
        }
      }
    }

    describe("with distributed-cache spec") {
      it("no Cache binding") {
        withCache[String, Integer]("distributedCache", 3) { cache =>
          (1 to 5).foreach(i => cache.put(s"key$i", i))

          val scriptingManager =
            cache.getCacheManager.getGlobalComponentRegistry.getComponent(classOf[ScriptingManager])

          scriptingManager.addScript("distNoCacheBindings.js", loadScript("scripts/distNoCacheBindings.js"))

          val thrown = the[IllegalStateException] thrownBy scriptingManager.runScript[String]("distNoCacheBindings.js")
          thrown.getMessage should include("ISPN021009: Distributed script")
          thrown.getMessage should include("invoked without a cache binding")
        }
      }

      it("no Cache binding, with user parameters") {
        withCache[String, Integer]("distributedCache", 3) { cache =>
          (1 to 5).foreach(i => cache.put(s"key$i", i))

          val scriptingManager =
            cache.getCacheManager.getGlobalComponentRegistry.getComponent(classOf[ScriptingManager])

          scriptingManager.addScript("distNoCacheWithBindings.js", loadScript("scripts/distNoCacheWithBindings.js"))

          val bindings = new SimpleBindings
          bindings.put("cacheName", "distributedCache")

          val thrown = the[IllegalStateException] thrownBy scriptingManager.runScript[util.List[_]]("distNoCacheWithBindings.js", bindings)
          thrown.getMessage should include("ISPN021009: Distributed script")
          thrown.getMessage should include("invoked without a cache binding")
          thrown.printStackTrace()
        }
      }

      it("with Cache binding") {
        withCache[String, Integer]("distributedCache", 3) { cache =>
          (1 to 5).foreach(i => cache.put(s"key$i", i))

          val scriptingManager =
            cache.getCacheManager.getGlobalComponentRegistry.getComponent(classOf[ScriptingManager])

          scriptingManager.addScript("distWithCacheBingings.js", loadScript("scripts/distWithCacheBingings.js"))

          val future = scriptingManager.runScript[util.List[Integer]]("distWithCacheBingings.js", cache)
          val list = new util.ArrayList[Integer]
          list.add(9)
          future.get should be(list)
        }
      }

      it("with Cache binding, with user parameters") {
        withCache[String, Integer]("distributedCache", 3) { cache =>
          (1 to 5).foreach(i => cache.put(s"key$i", i))

          val scriptingManager =
            cache.getCacheManager.getGlobalComponentRegistry.getComponent(classOf[ScriptingManager])

          scriptingManager.addScript("distWithCacheWithParameters.js", loadScript("scripts/distWithCacheWithParameters.js"))

          val bindings = new SimpleBindings
          bindings.put("prefix", "★")
          bindings.put("suffix", "★")

          val future = scriptingManager.runScript[Integer]("distWithCacheWithParameters.js", cache, bindings)

          val thrown = the[ExecutionException] thrownBy future.get
          thrown.getMessage should include("ISPN021003: Script execution error")
        }
      }
    }
  }
}

