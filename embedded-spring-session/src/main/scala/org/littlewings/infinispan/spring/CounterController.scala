package org.littlewings.infinispan.spring

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.{GetMapping, RestController}
import org.springframework.web.context.annotation.SessionScope

import scala.collection.JavaConverters._

@RestController
class CounterController(counter: Counter) {
  @GetMapping(Array("counter/access"))
  def access: java.util.Map[String, AnyRef] = {
    counter.increment()
    Map[String, AnyRef](
      "value" -> Integer.valueOf(counter.value),
      "time" -> counter.time.format(DateTimeFormatter.ISO_DATE_TIME)
    ).asJava
  }
}

@SessionScope
@Component
@SerialVersionUID(1L)
class Counter extends Serializable {
  var value: Int = 0

  val time: LocalDateTime = LocalDateTime.now

  def increment(): Unit = value += 1
}
