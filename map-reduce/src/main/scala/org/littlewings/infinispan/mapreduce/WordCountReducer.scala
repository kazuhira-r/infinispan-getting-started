package org.littlewings.infinispan.mapreduce

import scala.collection.JavaConverters._

import org.infinispan.distexec.mapreduce.Reducer

@SerialVersionUID(1L)
class WordCountReducer extends Reducer[String, Int] with Serializable {
  override def reduce(key: String, iterator: java.util.Iterator[Int]): Int =
    iterator.asScala.foldLeft(0) { (acc, cur) => acc + cur }
}
