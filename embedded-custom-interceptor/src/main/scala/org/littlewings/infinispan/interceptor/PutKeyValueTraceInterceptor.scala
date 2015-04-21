package org.littlewings.infinispan.interceptor

import org.infinispan.commands.write.PutKeyValueCommand
import org.infinispan.context.InvocationContext
import org.infinispan.interceptors.base.BaseCustomInterceptor

class PutKeyValueTraceInterceptor extends BaseCustomInterceptor {
  override def visitPutKeyValueCommand(ctx: InvocationContext, command: PutKeyValueCommand): AnyRef = {
    println(s"[${getClass.getSimpleName}] Start. key = ${command.getKey}, value = ${command.getValue}")

    val result = super.visitPutKeyValueCommand(ctx, command)
    // もしくは
    // val result = invokeNextInterceptor(ctx, command)

    println(s"[${getClass.getSimpleName}] End. key = ${command.getKey}, value = ${command.getValue}")

    result
  }
}
