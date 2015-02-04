package org.littlewings.infinispan.mapreduce

import scala.collection.JavaConverters._

import org.infinispan.distexec.mapreduce.Collator

class TopWordCollator extends Collator[String, Int, String] {
  override def collate(reducedResults: java.util.Map[String, Int]): String =
    reducedResults
      .asScala
      .toVector
      .sortWith { case ((key1, value1), (key2, value2)) => value1 > value2 }
      .head._1
}
