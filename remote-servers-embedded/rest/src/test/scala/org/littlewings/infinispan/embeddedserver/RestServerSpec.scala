package org.littlewings.infinispan.embeddedserver

import javax.ws.rs.client.{ClientBuilder, Entity}
import javax.ws.rs.core.Response

import org.infinispan.configuration.cache.ConfigurationBuilder
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.rest.NettyRestServer
import org.infinispan.rest.configuration.RestServerConfigurationBuilder
import org.scalatest.{FunSpec, Matchers}

class RestServerSpec extends FunSpec with Matchers {
  describe("Rest Embedded Server") {
    it("getting started") {
      // Embedded Cache setup
      val embeddedCacheManager = new DefaultCacheManager
      embeddedCacheManager.defineConfiguration("restCache", new ConfigurationBuilder().build)

      // Rest Server setup
      val restServerHost = "localhost"
      val restServerPort = 8080
      val restServer =
        NettyRestServer(
          new RestServerConfigurationBuilder()
            .host(restServerHost)
            .port(restServerPort)
            .build(),
          embeddedCacheManager
        )
      restServer.start()

      // use Cache
      val book = Book("978-1782169970", "Infinispan Data Grid Platform Definitive Guide", 5337)

      val client = ClientBuilder.newBuilder().build

      val putResponse =
        client
          .target(s"http://${restServerHost}:${restServerPort}/rest/restCache/${book.isbn}")
          .request
          .put(Entity.json(book))
      putResponse.getStatus should be(Response.Status.OK.getStatusCode)
      putResponse.close()

      val responseBook =
        client
        .target(s"http://${restServerHost}:${restServerPort}/rest/restCache/${book.isbn}")
        .request
          .get(classOf[Book])

      responseBook.title should be("Infinispan Data Grid Platform Definitive Guide")
      responseBook.price should be(5337)

      // resource clean up
      client.close()
      restServer.stop()
      embeddedCacheManager.stop()
    }
  }
}
