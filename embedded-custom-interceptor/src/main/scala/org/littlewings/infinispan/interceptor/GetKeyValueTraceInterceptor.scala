package org.littlewings.infinispan.interceptor

import org.infinispan.commands.read.GetKeyValueCommand
import org.infinispan.context.InvocationContext
import org.infinispan.interceptors.base.BaseCustomInterceptor

class GetKeyValueTraceInterceptor extends BaseCustomInterceptor {
  override def visitGetKeyValueCommand(ctx: InvocationContext, command: GetKeyValueCommand): AnyRef = {
    println(s"[${getClass.getSimpleName}] Start. key = ${command.getKey}")

    val result = super.visitGetKeyValueCommand(ctx, command)
    // もしくは
    // val result = invokeNextInterceptor(ctx, command)

    println(s"[${getClass.getSimpleName}] End. key = ${command.getKey}, value = ${result}")

    result
  }
}
