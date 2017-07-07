package org.littlewings.infinispan.spring

import org.infinispan.spring.session.configuration.EnableInfinispanRemoteHttpSession
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

object App {
  def main(args: Array[String]): Unit = {
    SpringApplication.run(classOf[App], args: _*)
  }
}

@EnableInfinispanRemoteHttpSession
@SpringBootApplication
class App
