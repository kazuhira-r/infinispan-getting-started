package org.littlewings.infinispan.scripting

import java.io.{BufferedReader, InputStreamReader}
import java.nio.charset.StandardCharsets

import org.infinispan.Cache
import org.infinispan.manager.DefaultCacheManager
import org.scalatest.Suite

trait InfinispanSpecSupport extends Suite {
  protected def withCache[K, V](cacheName: String, numInstances: Int = 1)(f: Cache[K, V] => Unit): Unit = {
    val managers = (1 to numInstances).map(_ => new DefaultCacheManager("infinispan.xml"))

    try {
      managers.foreach(_.getCache[K, V](cacheName))

      val cache = managers.head.getCache[K, V](cacheName)
      f(cache)

      cache.stop()
    } finally {
      managers.foreach(_.stop())
    }
  }

  protected def loadScript(filePath: String): String = {
    val classLoader = Thread.currentThread.getContextClassLoader match {
      case null => getClass.getClassLoader
      case cl => cl
    }

    val reader = new BufferedReader(new InputStreamReader(classLoader.getResourceAsStream(filePath), StandardCharsets.UTF_8))
    try {
      Iterator
        .continually(reader.read())
        .takeWhile(_ != -1)
        .map(_.asInstanceOf[Char])
        .mkString
    } finally {
      reader.close()
    }
  }
}
