package io.duna.core.proxy.internal

import co.paralleluniverse.fibers.Suspendable
import io.duna.asm.ClassVisitor
import io.duna.asm.Handle
import io.duna.asm.Opcodes.*
import io.duna.asm.Type
import io.duna.asm.Type.*
import io.duna.asm.commons.GeneratorAdapter
import io.duna.asm.commons.Method
import io.duna.core.io.BufferOutputStream
import io.duna.core.proxy.internal.ASMTypes.*
import net.bytebuddy.agent.builder.LambdaFactory
import java.lang.invoke.LambdaMetafactory
import java.lang.reflect.Method as JMethod

object ProxyMethodBytecodeGenerator {

  val metafactoryMethod = LambdaFactory::class.java
      .declaredMethods.find { it.name == "metafactory" }

  fun forMethod(method: Method,
                classVisitor: ClassVisitor,
                classInternalName: String,
                remoteAddress: String) {

    val lambdaMethod = generateLambdaMethod(method, classVisitor,
        classInternalName, remoteAddress)

    val methodVisitor = classVisitor.visitMethod(ACC_PUBLIC,
        method.name, method.descriptor,
        /* signature */ null, /* exceptions */ null)

    val adapter = GeneratorAdapter(methodVisitor, ACC_PUBLIC,
        method.name, method.descriptor)

    val an = adapter.visitAnnotation(getDescriptor(Suspendable::class.java), true)
    an.visitEnd()

    val l0 = adapter.mark()

    // Local Variables
    val locOutBuffer = adapter.newLocal(BUFFER_OUTPUT_STREAM)
    val locInBuffer = adapter.newLocal(BUFFER_INPUT_STREAM)

    val locGenerator = adapter.newLocal(JSON_GENERATOR)
    val locParser = adapter.newLocal(JSON_PARSER_TYPE)

    val locResult = adapter.newLocal(MESSAGE_TYPE)

    adapter.newInstance(getType(BufferOutputStream::class.java))
    adapter.dup()

    adapter.push(1024) // Default buffer size
    adapter.invokeConstructor(BUFFER_OUTPUT_STREAM,
        Method("<init>", VOID_TYPE, arrayOf<Type>(INT_TYPE)))
    adapter.storeLocal(locOutBuffer)

    adapter.loadThis()
    adapter.getField(getObjectType(classInternalName), "objectMapper", OBJECT_MAPPER)
    adapter.invokeVirtual(OBJECT_MAPPER, Method("getFactory", JSON_FACTORY, null))

    adapter.loadLocal(locOutBuffer)
    adapter.invokeVirtual(JSON_FACTORY, Method("createGenerator", JSON_GENERATOR, null))
    adapter.storeLocal(locGenerator)

    adapter.loadLocal(locGenerator)
    adapter.invokeVirtual(JSON_GENERATOR, Method("writeStartArray", VOID_TYPE, arrayOf()))

    method.argumentTypes.forEachIndexed { i, it ->
      adapter.loadLocal(locGenerator)
      adapter.loadArg(i)

      if (it.sort !in arrayOf(ARRAY, OBJECT, METHOD)) {
        adapter.box(it)
      }

      adapter.invokeVirtual(JSON_GENERATOR,
          Method("writeObject", VOID_TYPE, arrayOf(OBJECT_TYPE)))
    }

    adapter.invokeVirtual(JSON_GENERATOR, Method("writeEndArray", VOID_TYPE, arrayOf()))
    adapter.invokeVirtual(JSON_GENERATOR, Method("flush", VOID_TYPE, arrayOf()))

    adapter.loadThis()
    adapter.loadLocal(locOutBuffer)
    adapter.invokeDynamic("accept",
        getMethodDescriptor(CONSUMER_TYPE, getObjectType(classInternalName), BUFFER_OUTPUT_STREAM),
        getMetafactoryHandle(),
        arrayOf(
            getType(getMethodDescriptor(VOID_TYPE, OBJECT_TYPE)),
            getLambdaMethodHandle(lambdaMethod, classInternalName),
            getType(getMethodDescriptor(VOID_TYPE, VERTX_HANDLER_TYPE))
        ))

    adapter.invokeStatic(SYNC_TYPE, Method("awaitResult", OBJECT_TYPE, arrayOf(CONSUMER_TYPE)))
    adapter.checkCast(MESSAGE_TYPE)
    adapter.storeLocal(locResult)

    adapter.newInstance(BUFFER_INPUT_STREAM)
    adapter.dup()
    adapter.loadLocal(locResult)
    adapter.invokeInterface(MESSAGE_TYPE, Method("body", OBJECT_TYPE, arrayOf()))
    adapter.invokeConstructor(BUFFER_INPUT_STREAM, Method("<init>", VOID_TYPE, arrayOf(BUFFER_TYPE)))
    adapter.storeLocal(locInBuffer)

    adapter.loadThis()
    adapter.getField(getObjectType(classInternalName), "objectMapper", OBJECT_MAPPER)
    adapter.invokeVirtual(OBJECT_MAPPER, Method("getFactory", JSON_FACTORY, null))

    adapter.loadLocal(locInBuffer)
    adapter.invokeVirtual(JSON_FACTORY, Method("createParser", JSON_PARSER_TYPE,
        arrayOf(INPUT_STREAM_TYPE)))
    adapter.storeLocal(locParser)

    adapter.loadLocal(locParser)
    adapter.push(method.returnType.descriptor)
    adapter.invokeVirtual(JSON_PARSER_TYPE, Method("readValueAs", OBJECT_TYPE, arrayOf(CLASS_TYPE)))
    adapter.checkCast(method.returnType)
    adapter.returnValue()
    adapter.endMethod()
  }

  private fun generateLambdaMethod(method: Method,
                                   classVisitor: ClassVisitor,
                                   classInternalName: String,
                                   remoteAddress: String): Method {

    val methodVisitor = classVisitor.visitMethod(ACC_PUBLIC, "lambda\$call\$${method.name}",
        getMethodDescriptor(VOID_TYPE, BUFFER_OUTPUT_STREAM, VERTX_HANDLER_TYPE),
        /* signature */ null, /* exceptions */ null)

    val adapter = GeneratorAdapter(methodVisitor, ACC_PUBLIC,
        method.name, method.descriptor)



    return Method("lambda\$call\$${method.name}",
        getMethodDescriptor(VOID_TYPE, BUFFER_OUTPUT_STREAM, VERTX_HANDLER_TYPE))
  }

//  private fun createMethodAdapter(classVisitor: ClassVisitor, access: Int,
//                                  name: String, descriptor: String): GeneratorAda {
//
//  }

  private fun getMetafactoryHandle(): Handle {
    return Handle(H_INVOKESTATIC, getInternalName(LambdaMetafactory::class.java),
        metafactoryMethod?.name, getMethodDescriptor(metafactoryMethod),
        false)
  }

  private fun getLambdaMethodHandle(lambdaMethod: Method, classInternalName: String): Handle {
    return Handle(H_INVOKESPECIAL, classInternalName, lambdaMethod.name,
        lambdaMethod.descriptor, false)
  }
}