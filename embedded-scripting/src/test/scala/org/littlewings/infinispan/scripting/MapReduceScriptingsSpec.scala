package org.littlewings.infinispan.scripting

import java.util

import org.infinispan.scripting.ScriptingManager
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class MapReduceScriptingsSpec extends FunSpec with InfinispanSpecSupport {
  describe("Infinispan Map Reduce Scripting Spec") {
    it("word count") {
      withCache[String, String]("localCache") { cache =>
        cache.put("1", "Hello world here I am")
        cache.put("2", "Infinispan rules the world")
        cache.put("3", "JUDCon is in Boston")
        cache.put("4", "JBoss World is in Boston as well")
        cache.put("12", "JBoss Application Server")
        cache.put("15", "Hello world")
        cache.put("14", "Infinispan community")
        cache.put("15", "Hello world")

        cache.put("111", "Infinispan open source")
        cache.put("112", "Boston is close to Toronto")
        cache.put("113", "Toronto is a capital of Ontario")
        cache.put("114", "JUDCon is cool")
        cache.put("211", "JBoss World is awesome")
        cache.put("212", "JBoss rules")
        cache.put("213", "JBoss division of RedHat ")
        cache.put("214", "RedHat community")

        val scriptingManager =
          cache.getCacheManager.getGlobalComponentRegistry.getComponent(classOf[ScriptingManager])

        scriptingManager.addScript("wordCountMapper.js", loadScript("scripts/wordCountMapper.js"))
        scriptingManager.addScript("wordCountReducer.js", loadScript("scripts/wordCountReducer.js"))
        scriptingManager.addScript("wordCountCollator.js", loadScript("scripts/wordCountCollator.js"))

        val future = scriptingManager.runScript[util.Map[String, Double]]("wordCountMapper.js", cache)
        val result = future.get

        result should be(a[util.LinkedHashMap[_, _]])

        result.get("boston") should be(3.0)
        result.get("infinispan") should be(3.0)
        result.get("toronto") should be(2.0)
        result.get("judcon") should be(2.0)
        result.get("community") should be(2.0)
      }
    }

    it("word count, using Groovy") {
      withCache[String, String]("localCache") { cache =>
        cache.put("1", "Hello world here I am")
        cache.put("2", "Infinispan rules the world")
        cache.put("3", "JUDCon is in Boston")
        cache.put("4", "JBoss World is in Boston as well")
        cache.put("12", "JBoss Application Server")
        cache.put("15", "Hello world")
        cache.put("14", "Infinispan community")
        cache.put("15", "Hello world")

        cache.put("111", "Infinispan open source")
        cache.put("112", "Boston is close to Toronto")
        cache.put("113", "Toronto is a capital of Ontario")
        cache.put("114", "JUDCon is cool")
        cache.put("211", "JBoss World is awesome")
        cache.put("212", "JBoss rules")
        cache.put("213", "JBoss division of RedHat ")
        cache.put("214", "RedHat community")

        val scriptingManager =
          cache.getCacheManager.getGlobalComponentRegistry.getComponent(classOf[ScriptingManager])

        scriptingManager.addScript("wordCountMapper.groovy", loadScript("scripts/wordCountMapper.groovy"))
        scriptingManager.addScript("wordCountReducer.groovy", loadScript("scripts/wordCountReducer.groovy"))
        scriptingManager.addScript("wordCountCollator.groovy", loadScript("scripts/wordCountCollator.groovy"))

        val future = scriptingManager.runScript[util.Map[String, Double]]("wordCountMapper.groovy", cache)
        val result = future.get

        result should be(a[util.LinkedHashMap[_, _]])

        result.get("boston") should be(3.0)
        result.get("infinispan") should be(3.0)
        result.get("toronto") should be(2.0)
        result.get("judcon") should be(2.0)
        result.get("community") should be(2.0)
      }
    }
  }
}
