package org.littlewings.keypartitioner

import org.infinispan.commons.util.Util
import org.infinispan.configuration.cache.HashConfiguration
import org.infinispan.distribution.ch.KeyPartitioner

class MyKeyPartitioner extends KeyPartitioner {
  var configuration: HashConfiguration = _

  override def init(configuration: HashConfiguration): Unit = {
    this.configuration = configuration
  }

  override def getSegment(key: AnyRef): Int =
    key.asInstanceOf[String].split("-")(1).toInt % configuration.numSegments
}
