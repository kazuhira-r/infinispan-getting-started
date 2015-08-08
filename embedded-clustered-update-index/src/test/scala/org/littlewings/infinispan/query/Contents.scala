package org.littlewings.infinispan.query

import java.util.Objects

import org.hibernate.search.annotations.{Field, Indexed}

object Contents {
  def apply(id: String, value: String): Contents = {
    val c = new Contents
    c.id = id
    c.value = value
    c
  }
}

@Indexed
@SerialVersionUID(1L)
class Contents extends Serializable {
  @Field
  var id: String = _

  @Field
  var value: String = _

  override def hashCode: Int = Objects.hash(id, value)

  override def equals(o: Any): Boolean = o match {
    case other: Contents => Objects.equals(id, other.id) && Objects.equals(value, other.value)
    case _ => false
  }
}
