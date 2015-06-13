package org.littlewings.infinispan.entryretrieval

import org.infinispan.filter.AbstractKeyValueFilterConverter
import org.infinispan.metadata.Metadata

@SerialVersionUID(1L)
class EvenDoublingKeyValueFilterConverter
    extends AbstractKeyValueFilterConverter[String, Integer, Integer]
    with Serializable {
  override def filterAndConvert(key: String, value: Integer, metadata: Metadata): Integer =
    if ((key.replaceAll("key", "").toInt % 2) == 0)
      value * 2
    else
      null
}
