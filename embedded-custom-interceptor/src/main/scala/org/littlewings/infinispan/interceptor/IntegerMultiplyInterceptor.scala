package org.littlewings.infinispan.interceptor

import scala.beans.BeanProperty

import org.infinispan.commands.write.PutKeyValueCommand
import org.infinispan.context.InvocationContext
import org.infinispan.interceptors.base.BaseCustomInterceptor

class IntegerMultiplyInterceptor extends BaseCustomInterceptor {
  @BeanProperty
  var num: String = "1" 

  override def visitPutKeyValueCommand(ctx: InvocationContext, command: PutKeyValueCommand): AnyRef = {
    val newValue: AnyRef = command.getValue match {
      case n: Integer => Integer.valueOf(n * num.toInt)
      case n => n
    }

    command.setValue(newValue)

    super.visitPutKeyValueCommand(ctx, command)
    // もしくは
    // invokeNextInterceptor(ctx, command)
  }
}
