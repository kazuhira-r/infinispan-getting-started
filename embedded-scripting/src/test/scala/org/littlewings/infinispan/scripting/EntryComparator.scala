package org.littlewings.infinispan.scripting

import java.util
import java.util.Comparator

class EntryComparator extends Comparator[util.Map.Entry[String, Double]] {
  override def compare(o1: util.Map.Entry[String, Double], o2: util.Map.Entry[String, Double]): Int =
    if (o1.getValue < o2.getValue) {
      1
    } else {
      if (o1.getValue > o2.getValue) {
        -1
      } else {
        0
      }
    }
}

class EntryComparatorInGroovy extends Comparator[util.Map.Entry[String, Integer]] {
  override def compare(o1: util.Map.Entry[String, Integer], o2: util.Map.Entry[String, Integer]): Int =
    if (o1.getValue < o2.getValue) {
      1
    } else {
      if (o1.getValue > o2.getValue) {
        -1
      } else {
        0
      }
    }
}
