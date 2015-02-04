package org.littlewings.infinispan.mapreduce

import org.infinispan.distexec.mapreduce.{ Collector, Mapper }

@SerialVersionUID(1L)
class WordCountMaper extends Mapper[String, String, String, Int] with Serializable {
  override def map(key: String, value: String, collector: Collector[String, Int]): Unit =
    value.split("""\s+""").foreach(w => collector.emit(w, 1))
}
