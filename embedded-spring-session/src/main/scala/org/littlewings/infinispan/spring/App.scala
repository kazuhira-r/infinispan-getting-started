package org.littlewings.infinispan.spring

import org.infinispan.spring.session.configuration.EnableInfinispanEmbeddedHttpSession
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@EnableInfinispanEmbeddedHttpSession
@SpringBootApplication
class App

object App {
  def main(args: Array[String]): Unit = {
    SpringApplication.run(classOf[App], args: _*)
  }
}
