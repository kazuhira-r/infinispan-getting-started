package org.littlewings.infinispan.interceptor

import org.infinispan.commands.read.GetKeyValueCommand
import org.infinispan.commands.write.PutKeyValueCommand
import org.infinispan.context.InvocationContext
import org.infinispan.interceptors.base.BaseCustomInterceptor

class PutGetTraceInterceptor extends BaseCustomInterceptor {
  override def visitGetKeyValueCommand(ctx: InvocationContext, command: GetKeyValueCommand): AnyRef = {
    println(s"[${getClass.getSimpleName}] Start. key = ${command.getKey}")

    val result = super.visitGetKeyValueCommand(ctx, command)
    // もしくは
    // val result = invokeNextInterceptor(ctx, command)

    println(s"[${getClass.getSimpleName}] End. key = ${command.getKey}, value = ${result}")

    result
  }

  override def visitPutKeyValueCommand(ctx: InvocationContext, command: PutKeyValueCommand): AnyRef = {
    println(s"[${getClass.getSimpleName}] Start. key = ${command.getKey}, value = ${command.getValue}")

    val result = super.visitPutKeyValueCommand(ctx, command)
    // もしくは
    // val result = invokeNextInterceptor(ctx, command)

    println(s"[${getClass.getSimpleName}] End. key = ${command.getKey}, value = ${command.getValue}")

    result
  }
}
