package org.littlewings.infinispan.scattered

import org.infinispan.configuration.cache.HashConfiguration
import org.infinispan.distribution.ch.KeyPartitioner

class SimpleKeyPartitioner extends KeyPartitioner {
  var configuration: HashConfiguration = _

  override def init(configuration: HashConfiguration): Unit = {
    this.configuration = configuration
  }

  override def getSegment(key: Any): Int =
    (Integer.parseInt(key.asInstanceOf[String].replaceAll("""key(\d+)-\d+""", "$1")) * 150 + 73) % configuration.numSegments
}
