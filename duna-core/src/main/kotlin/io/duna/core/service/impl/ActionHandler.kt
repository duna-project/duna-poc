package io.duna.core.service.impl

import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.dynamic.scaffold.InstrumentedType
import net.bytebuddy.implementation.Implementation
import net.bytebuddy.implementation.MethodCall
import net.bytebuddy.implementation.bytecode.ByteCodeAppender
import net.bytebuddy.implementation.bytecode.StackManipulation
import net.bytebuddy.jar.asm.MethodVisitor

internal class ActionHandler : Implementation {

  override fun prepare(instrumentedType: InstrumentedType?): InstrumentedType {
    throw UnsupportedOperationException("not implemented")

//    ByteBuddy()
//    .subclass(Any::class.java)
//    .implement(Any::class.java)
//    .defineMethod("invoke", Any::class.java, Visibility.PUBLIC)
//      .withParameters(Array<Any>::class.java)
//    .intercept(StackManipulation.Compound(
//        MethodCall.invoke(MethodDescription.UNDEFINED)
//            .on(target)
//            .with("asd")
//    ))
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