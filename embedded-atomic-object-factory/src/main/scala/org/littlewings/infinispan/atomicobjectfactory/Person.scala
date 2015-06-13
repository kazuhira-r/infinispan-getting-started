package org.littlewings.infinispan.atomicobjectfactory

import java.io.{ObjectInput, IOException, ObjectOutput}

import org.infinispan.atomic.{Updatable, Update}

@SerialVersionUID(1L)
class Person(private var firstName: String,
             private var lastName: String,
             private var age: Integer) extends Updatable {
  def this() = this(null, null, 0)

  @Update
  def setFirstName(firstName: String): Unit =
    this.firstName = firstName

  def getFirstName: String = firstName

  @Update
  def setLastName(lastName: String): Unit =
    this.lastName = lastName

  def getLastName: String = lastName

  @Update
  def setAge(age: Integer): Unit =
    this.age = age

  def getAge: Integer = age

  @throws(classOf[IOException])
  override def writeExternal(out: ObjectOutput): Unit = {
    out.writeUTF(firstName)
    out.writeUTF(lastName)
    out.writeInt(age)
  }

  @throws(classOf[IOException])
  override def readExternal(in: ObjectInput): Unit = {
    firstName = in.readUTF()
    lastName = in.readUTF()
    age = in.readInt()
  }
}
