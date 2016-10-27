package io.duna.core.service.impl

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.dynamic.scaffold.InstrumentedType
import net.bytebuddy.implementation.Implementation
import net.bytebuddy.implementation.bytecode.ByteCodeAppender
import net.bytebuddy.jar.asm.MethodVisitor

internal class ActionHandler : Implementation {
  override fun prepare(instrumentedType: InstrumentedType?): InstrumentedType {
    throw UnsupportedOperationException("not implemented")
  }

  override fun appender(implementationTarget: Implementation.Target?): ByteCodeAppender {
    return Appender()
  }

  inner class Appender : ByteCodeAppender {
    override fun apply(methodVisitor: MethodVisitor?, implementationContext: Implementation.Context?,
                       instrumentedMethod: MethodDescription?): ByteCodeAppender.Size {
      throw UnsupportedOperationException("not implemented")
    }
  }
}