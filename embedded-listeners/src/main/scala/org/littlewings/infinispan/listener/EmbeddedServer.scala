package org.littlewings.infinispan.listener

import java.io.{InputStreamReader, BufferedReader}
import java.nio.charset.StandardCharsets

import org.infinispan.manager.DefaultCacheManager

object EmbeddedServer {
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

      System.console().readLine("> Press Enter, stop!")

      cache.stop()
    } finally {
      manager.stop()
    }
  }
}
