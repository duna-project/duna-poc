package io.duna.core.proxy.impl

import net.bytebuddy.dynamic.scaffold.InstrumentedType
import net.bytebuddy.implementation.Implementation
import net.bytebuddy.implementation.bytecode.ByteCodeAppender

object ProxyMethodImplementation : Implementation {
  override fun prepare(instrumentedType: InstrumentedType): InstrumentedType {
    return instrumentedType
  }

  override fun appender(implementationTarget: Implementation.Target): ByteCodeAppender {
    return ProxyMethodBytecodeAppender
  }
}