package org.littlewings.infinispan.compatibility

import java.util.Objects

class Person(val name: String, val age: Int) extends Serializable {
  override def equals(o: Any): Boolean =
  o match {
    case other: Person => Objects.equals(name, other.name) && Objects.equals(age, other.age)
    case _ => false
  }

  override def hashCode: Int = Objects.hash(name, Integer.valueOf(age))
}
