package org.littlewings.infinispan.clustering

import scala.collection.JavaConverters._
import scala.io.StdIn

import org.infinispan.manager.DefaultCacheManager

object ClusteringCacheDemo {
  def main(args: Array[String]): Unit = {
    val (nodeName, cacheName) = (args(0), args(1))

    System.setProperty("nodeName", nodeName)

    val manager = new DefaultCacheManager("infinispan.xml")
    val cache = manager.getCache[String, String](cacheName)

    Iterator
      .continually(StdIn.readLine(s"$nodeName> "))
      .withFilter(l => l != null && !l.isEmpty)
      .takeWhile(_ != "exit")
      .map(_.split(" +").toList)
      .foreach {
        case Nil =>
        case "put" :: key :: value :: Nil =>
          cache.put(key, value)
          println(s"Putted, $key : $value")
        case "get" :: key :: Nil =>
          println(s"Get, Key[$key] => ${cache.get(key)}")
        case "size" :: Nil =>
          println(s"Size = ${cache.size}")
        case "keys" :: Nil =>
          println(s"Keys:")
          cache.keySet.asScala.foreach(k => println(s" $k"))
        case "locate" :: Nil =>
          println("Locate:")
          val dm = cache.getAdvancedCache.getDistributionManager
          cache.keySet.asScala.foreach { k =>
            val primary = dm.getPrimaryLocation(k)
            val locate = dm.locate(k)
            println(s" Key[$k]  Primary: $primary, Locate: $locate")
          }
        case command =>
          println(s"Unknown command, [${command(0)}]")
      }

    println(s"Exit CacheServer[$nodeName]")

    cache.stop()
    manager.stop()
  }
}
