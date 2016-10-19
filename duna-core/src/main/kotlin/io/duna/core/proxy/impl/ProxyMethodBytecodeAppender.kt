package io.duna.core.proxy.impl

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import io.duna.core.bytecode.MethodVariableStore
import io.duna.core.io.BufferOutputStream
import net.bytebuddy.agent.builder.LambdaFactory
import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.implementation.Implementation
import net.bytebuddy.implementation.bytecode.ByteCodeAppender
import net.bytebuddy.implementation.bytecode.ByteCodeAppender.Size
import net.bytebuddy.implementation.bytecode.Duplication
import net.bytebuddy.implementation.bytecode.StackManipulation
import net.bytebuddy.implementation.bytecode.TypeCreation
import net.bytebuddy.implementation.bytecode.assign.TypeCasting
import net.bytebuddy.implementation.bytecode.assign.reference.ReferenceTypeAwareAssigner
import net.bytebuddy.implementation.bytecode.constant.IntegerConstant
import net.bytebuddy.implementation.bytecode.member.FieldAccess
import net.bytebuddy.implementation.bytecode.member.MethodInvocation
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess
import net.bytebuddy.jar.asm.MethodVisitor
import net.bytebuddy.matcher.ElementMatchers
import net.bytebuddy.matcher.ElementMatchers.*
import java.io.OutputStream
import java.util.*

object ProxyMethodBytecodeAppender : ByteCodeAppender {

  override fun apply(methodVisitor: MethodVisitor,
                     implementationContext: Implementation.Context,
                     instrumentedMethod: MethodDescription): Size {

    // Parameter count + this
    val firstLocalVar = instrumentedMethod.parameters
        .map { it.type.stackSize.toIncreasingSize() }
        .reduce { a, b -> a.aggregate(b) }
        .sizeImpact

    val localVar = { offset: Int -> firstLocalVar + offset }

    // new BufferOutputStream
    val setupStack = StackManipulation.Compound(
        TypeCreation.of(TypeDescription.ForLoadedType(BufferOutputStream::class.java)),
        Duplication.SINGLE,

        IntegerConstant.forValue(1024),
        MethodInvocation.invoke(
            TypeDescription.ForLoadedType(BufferOutputStream::class.java)
            .declaredMethods
            .filter(isConstructor<MethodDescription>().and(takesArguments(Int::class.java)))
            .only),
        MethodVariableStore.REFERENCE.storeOffset(localVar(0)),

        MethodVariableAccess.REFERENCE.loadOffset(0),
        FieldAccess.forField(
            implementationContext.instrumentedType
              .declaredFields.filter(named("objectMapper")).only).getter(),
        MethodInvocation.invoke(
            TypeDescription.ForLoadedType(ObjectMapper::class.java)
                .declaredMethods.filter(named("getFactory")).only),

        MethodVariableAccess.REFERENCE.loadOffset(localVar(0)),
        MethodInvocation.invoke(
            TypeDescription.ForLoadedType(ObjectMapper::class.java)
                .declaredMethods
                .filter(named<MethodDescription>("getFactory").and(takesArgument(0, OutputStream::class.java)))
                .only),
        MethodVariableStore.REFERENCE.storeOffset(localVar(1)),

        MethodVariableAccess.REFERENCE.loadOffset(localVar(1)),
        MethodInvocation.invoke(
            TypeDescription.ForLoadedType(JsonGenerator::class.java)
                .declaredMethods
                .filter(named<MethodDescription>("writeStartArray").and(takesArguments(0)))
                .only)
    )

    val parameterSerialization = LinkedList<StackManipulation>()

    instrumentedMethod.parameters.forEachIndexed { i, parameter ->
      parameterSerialization.push(MethodVariableAccess.REFERENCE.loadOffset(localVar(1)))
      parameterSerialization.push(MethodVariableAccess.REFERENCE.loadOffset(i + 1))

      if (parameter.type.isPrimitive) {
        parameterSerialization.push(MethodInvocation.invoke(
            parameter.type.declaredMethods
                .filter(named<MethodDescription>("valueOf").and(takesArguments(1)))
                .only
        ))
      }

      MethodInvocation.invoke(
          TypeDescription.ForLoadedType(JsonGenerator::class.java)
              .declaredMethods
              .filter(named<MethodDescription>("writeObject").and(takesArguments(1)))
              .only)
    }

    val parameterSerializationStack = StackManipulation.Compound(parameterSerialization)

    val messageSendingStack = StackManipulation.Compound(
        MethodVariableAccess.REFERENCE.loadOffset(localVar(1)),
        MethodInvocation.invoke(
            TypeDescription.ForLoadedType(JsonGenerator::class.java)
                .declaredMethods
                .filter(named<MethodDescription>("writeEndArray").and(takesArguments(0)))
                .only),

        MethodVariableAccess.REFERENCE.loadOffset(localVar(1)),
        MethodInvocation.invoke(
            TypeDescription.ForLoadedType(JsonGenerator::class.java)
                .declaredMethods
                .filter(named<MethodDescription>("flush").and(takesArguments(0)))
                .only),

        MethodVariableAccess.REFERENCE.loadOffset(0),
        MethodVariableAccess.REFERENCE.loadOffset(localVar(0))
    )

    return Size(0, 0)
  }
}