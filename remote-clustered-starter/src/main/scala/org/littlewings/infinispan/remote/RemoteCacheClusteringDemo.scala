package org.littlewings.infinispan.remote

import scala.collection.JavaConverters._
import scala.io.StdIn

import org.infinispan.client.hotrod.RemoteCacheManager
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder

object RemoteCacheClusteringDemo {
  def main(args: Array[String]): Unit = {
    val manager =
      new RemoteCacheManager(new ConfigurationBuilder()
        .addServers("localhost:11222;localhost:12222;localhsot:13222")
        // .addServers("localhost:11222")  // これでもOK
        .build)
    val cache = manager.getCache[String, String]("namedCache")

    Iterator
      .continually(StdIn.readLine("> "))
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
        case command =>
          println(s"Unknown command, [${command(0)}]")
      }

    cache.stop()
    manager.stop()
  }
}
