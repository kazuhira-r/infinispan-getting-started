package org.littlewings.infinispan.entryretrieval

import org.infinispan.filter.KeyValueFilter
import org.infinispan.metadata.Metadata

@SerialVersionUID(1L)
class EvenKeyValueFilter extends KeyValueFilter[String, Integer] with Serializable {
  override def accept(key: String, value: Integer, metadata: Metadata): Boolean =
    key.replaceAll("key", "").toInt % 2 == 0
}
