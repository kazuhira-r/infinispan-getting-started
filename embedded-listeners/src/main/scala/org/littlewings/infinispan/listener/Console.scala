package org.littlewings.infinispan.listener

import org.infinispan.manager.DefaultCacheManager

import scala.collection.JavaConverters._

object Console {
  def main(args: Array[String]): Unit = {
    System.setProperty("java.net.preferIPv4Stack", "true")

    val name :: mode :: Nil = args.toList

    val manager = new DefaultCacheManager("infinispan.xml")

    try {
      val cache = manager.getCache[String, String]("distCache")

      val listener =
        if (mode == "local") new LocalListener[String, String](name)
        else new ClusteredListener[String, String](name)

      cache.addListener(listener)

      Iterator
        .continually(System.console().readLine("> "))
        .filter(l => l != null && !l.isEmpty)
        .takeWhile(_ != "exit")
        .foreach { line =>
          line.split("\\s+").toList match {
            case "put" :: key :: value :: Nil =>
              cache.put(key, value)
              println(s"key:${key}, value = ${value}, putted.")
            case "get" :: key :: Nil =>
              println(s"get key = ${key} => value = ${cache.get(key)}")
            case "remove" :: key :: Nil =>
              cache.remove(key)
              println(s"removed key = ${key}")
            case "evict" :: key :: Nil =>
              cache.evict(key)
              println(s"evicted key = ${key}")
            case "all" :: Nil =>
              cache.entrySet.asScala.foreach(e => println(s"key = ${e.getKey}, value = ${e.getValue}"))
            case _ => println(s"unknown command[${line}]")
          }
        }
      cache.stop()
    } finally {
      manager.stop()
    }
  }
}
