package io.duna.core.proxy

import co.paralleluniverse.fibers.Suspendable
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import io.duna.asm.*
import io.duna.asm.Opcodes.*
import io.duna.asm.Type.*
import io.duna.core.io.BufferInputStream
import io.duna.core.io.BufferOutputStream
import io.duna.core.proxy.internal.DefaultServiceProxyNamingStrategy
import io.duna.core.service.Address
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.ext.sync.Sync
import java.io.InputStream
import java.io.OutputStream
import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method
import java.util.function.Consumer
import javax.inject.Inject

/**
 * A factory responsible for creating proxies for remote microservices.
 *
 * @author Carlos Eduardo Melo [ceduardo.melo@redime.com.br]
 */
class ServiceProxyFactory(val namingStrategy: ServiceProxyNamingStrategy) {

  constructor() : this(DefaultServiceProxyNamingStrategy())

  /**
   * Generates a binary JVM class proxying methods from the remote instance of a service.
   *
   * @param serviceClass the class of the service to be proxied.
   * @return the binary representation of the service proxy class.
   */
  fun generateFor(serviceClass: Class<*>): ByteArray {
    val cw = ClassWriter(0)

    val cv: ClassVisitor = cw //TraceClassVisitor(cw, PrintWriter(System.out))

    val proxyClassName = namingStrategy.getProxyClassName(serviceClass)
    val proxyClassInternalName = proxyClassName.replace('.', '/')
    val proxyClassDescriptor = "L${proxyClassInternalName};"

    val serviceAddress = serviceClass.getAnnotation(Address::class.java)?.value
        ?: serviceClass.canonicalName

    var fv: FieldVisitor
    var mv: MethodVisitor
    var av0: AnnotationVisitor

    cv.visit(V1_8, ACC_PUBLIC + ACC_FINAL + ACC_SUPER,
        proxyClassInternalName,
        null,
        getInternalName(Any::class.javaObjectType),
        arrayOf(getInternalName(serviceClass)))

    cv.visitInnerClass(getInternalName(MethodHandles::class.java) + "\$Lookup",
        getInternalName(MethodHandles::class.java),
        "Lookup",
        ACC_PUBLIC + ACC_FINAL + ACC_STATIC)

    run {
      fv = cv.visitField(ACC_PRIVATE, "vertx",
          getDescriptor(Vertx::class.java),
          getDescriptor(Vertx::class.java), null)

      av0 = fv.visitAnnotation(getDescriptor(Inject::class.java), true)
      av0.visitEnd()

      fv.visitEnd()
    }

    run {
      fv = cv.visitField(ACC_PRIVATE, "objectMapper",
          getDescriptor(ObjectMapper::class.java),
          getDescriptor(ObjectMapper::class.java),
          null)

      av0 = fv.visitAnnotation(getDescriptor(Inject::class.java), true)
      av0.visitEnd()

      fv.visitEnd()
    }

    // Constructor
    run {
      mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null)
      mv.visitCode()

      val l0 = Label()
      mv.visitLabel(l0)
      mv.visitVarInsn(ALOAD, 0)
      mv.visitMethodInsn(INVOKESPECIAL, getInternalName(Any::class.javaObjectType), "<init>", "()V", false)
      mv.visitInsn(RETURN)

      val l1 = Label()
      mv.visitLabel(l1)
      mv.visitLocalVariable("this", proxyClassDescriptor, null, l0, l1, 0)

      mv.visitMaxs(1, 1)
      mv.visitEnd()
    }

    serviceClass.methods.forEachIndexed { i, method ->
      generateMethod(cv, proxyClassInternalName, proxyClassDescriptor, i, method)
      generateMethodLambdaCall(cv, proxyClassInternalName, proxyClassDescriptor, serviceAddress, i)
    }

    cv.visitEnd()

    return cw.toByteArray()
  }

  private fun generateMethodLambdaCall(classVisitor: ClassVisitor,
                                       proxyClassInternalName: String,
                                       proxyClassDescriptor: String,
                                       serviceAddress: String,
                                       idx: Int) {
    val mv = classVisitor.visitMethod(ACC_PRIVATE + ACC_SYNTHETIC,
        "lambda\$call\$${idx}",
        "(${getDescriptor(BufferOutputStream::class.java)}${getDescriptor(Handler::class.java)})V",
        null, null)

    mv.visitCode()

    val lstart = Label()
    mv.visitLabel(lstart)

    mv.visitVarInsn(ALOAD, 0)
    mv.visitFieldInsn(GETFIELD, proxyClassInternalName, "vertx", getDescriptor(Vertx::class.java))
    mv.visitMethodInsn(INVOKEINTERFACE, getInternalName(Vertx::class.java),
        "eventBus",
        getMethodDescriptor(Vertx::class.java.getMethod("eventBus")),
        true)

    mv.visitLdcInsn(serviceAddress);
    mv.visitVarInsn(ALOAD, 1)
    mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(BufferOutputStream::class.java),
        "getBuffer",
        "()${getDescriptor(Buffer::class.java)}",
        false)

    mv.visitVarInsn(ALOAD, 2)
    mv.visitMethodInsn(INVOKEINTERFACE,
        getInternalName(EventBus::class.java),
        "send",
        getMethodDescriptor(EventBus::class.java.getMethod("send",
            String::class.java, Any::class.javaObjectType, Handler::class.java)),
        true)

    mv.visitInsn(POP)
    mv.visitInsn(RETURN)

    val lend = Label()
    mv.visitLabel(lend)

    mv.visitLocalVariable("this", proxyClassDescriptor, null, lstart, lend, 0)
    mv.visitLocalVariable("outputStream", getDescriptor(BufferOutputStream::class.java),
        null, lstart, lend, 0)
    mv.visitLocalVariable("h", getDescriptor(Handler::class.java), null, lstart, lend, 0)

    mv.visitMaxs(4, 3)

    mv.visitEnd()
  }

  private fun generateMethod(classVisitor: ClassVisitor,
                             proxyClassInternalName: String,
                             proxyClassDescriptor: String,
                             idx: Int,
                             method: Method) {

    // the reference to *this* is stored at index 0
    var firstNonParameterLocalVar = 1

    for (param in method.parameterTypes) {
      if (param.isAssignableFrom(Double::class.java) || param.isAssignableFrom(Long::class.java))
        firstNonParameterLocalVar += 2
      else
        firstNonParameterLocalVar++
    }

    val localVar = { i: Int -> firstNonParameterLocalVar + i }

    val mv = classVisitor.visitMethod(ACC_PUBLIC, method.name,
        getMethodDescriptor(method),
        null, // signature
        null)

    val an = mv.visitAnnotation(getDescriptor(Suspendable::class.java), true)
    an.visitEnd()

    mv.visitCode()

    val lstart = Label()
    val lend = Label()

    val ltry = Label()
    val lreturn = Label()
    val lcatch = Label()

    mv.visitLabel(lstart)

    mv.visitTryCatchBlock(ltry, lreturn, lcatch, getInternalName(Exception::class.java))
    mv.visitLabel(ltry)

    // Creates a new instance of BufferOutputStream
    mv.visitTypeInsn(NEW, getInternalName(BufferOutputStream::class.java))
    mv.visitInsn(DUP)

    // Invokes the constructor
    mv.visitIntInsn(SIPUSH, 1024)
    mv.visitMethodInsn(INVOKESPECIAL, getInternalName(BufferOutputStream::class.java), "<init>", "(I)V", false)
    mv.visitVarInsn(ASTORE, localVar(0))

    // Push the JsonFactory to the stack
    mv.visitVarInsn(ALOAD, 0) // this
    mv.visitFieldInsn(GETFIELD, proxyClassInternalName, "objectMapper", getDescriptor(ObjectMapper::class.java))
    mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(ObjectMapper::class.java), "getFactory",
        getMethodDescriptor(ObjectMapper::class.java.getMethod("getFactory")), false)

    // Create the JsonGenerator
    mv.visitVarInsn(ALOAD, localVar(0)) // outputStream
    mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(JsonFactory::class.java),
        "createGenerator",
        getMethodDescriptor(JsonFactory::class.java.getMethod("createGenerator", OutputStream::class.java)),
        false)
    mv.visitVarInsn(ASTORE, localVar(1))

    mv.visitVarInsn(ALOAD, localVar(1))
    mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(JsonGenerator::class.java),
        "writeStartArray",
        "()V",
        false)

    // Serialize the parameters into a Buffer
    method.parameterTypes.forEachIndexed { i, type ->
      mv.visitVarInsn(ALOAD, localVar(1)) // stack the generator
      mv.visitVarInsn(ALOAD, i + 1) // stack the parameter

      if (type.isPrimitive) {
        mv.visitMethodInsn(INVOKESTATIC,
            getInternalName(type),
            "valueOf",
            getMethodDescriptor(type.getMethod("valueOf", type)),
            false)
      }

      mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(JsonGenerator::class.java),
          "writeObject",
          "(Ljava/lang/Object;)V",
          false)
    }

    mv.visitVarInsn(ALOAD, localVar(1))
    mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(JsonGenerator::class.java),
        "writeEndArray",
        "()V",
        false)

    mv.visitVarInsn(ALOAD, localVar(1))
    mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(JsonGenerator::class.java),
        "flush",
        "()V",
        false)

    mv.visitVarInsn(ALOAD, 0)
    mv.visitVarInsn(ALOAD, localVar(0))

    // Call the remote serviceClass and wait for the result
    val llambda = Label()
    mv.visitLineNumber(50, llambda)
    mv.visitInvokeDynamicInsn("accept",
        "(${proxyClassDescriptor}${getDescriptor(BufferOutputStream::class.java)})${getDescriptor(Consumer::class.java)}",
        Handle(H_INVOKESTATIC, getInternalName(LambdaMetafactory::class.java), "metafactory",
            "(Ljava/lang/invoke/MethodHandles\$Lookup;" +
                "Ljava/lang/String;Ljava/lang/invoke/MethodType;" +
                "Ljava/lang/invoke/MethodType;" +
                "Ljava/lang/invoke/MethodHandle;" +
                "Ljava/lang/invoke/MethodType;)" +
                "Ljava/lang/invoke/CallSite;", false),
        Type.getType("(Ljava/lang/Object;)V"),
        Handle(Opcodes.H_INVOKESPECIAL, proxyClassInternalName,
            "lambda\$call\$${idx}",
            "(${getDescriptor(BufferOutputStream::class.java)}${getDescriptor(Handler::class.java)})V",
            false),
        Type.getType("(Lio/vertx/core/Handler;)V")
    )

    mv.visitMethodInsn(INVOKESTATIC, getInternalName(Sync::class.java),
        "awaitResult",
        getMethodDescriptor(Sync::class.java.getMethod("awaitResult", Consumer::class.java)),
        false)

    mv.visitTypeInsn(CHECKCAST, getInternalName(Message::class.java))
    mv.visitVarInsn(ASTORE, localVar(2))

    // Create a new BufferInputStream
    mv.visitTypeInsn(NEW, getInternalName(BufferInputStream::class.java))
    mv.visitInsn(DUP)
    mv.visitVarInsn(ALOAD, localVar(2))

    mv.visitMethodInsn(INVOKEINTERFACE, getInternalName(Message::class.java),
        "body",
        "()Ljava/lang/Object;",
        true)

    mv.visitTypeInsn(CHECKCAST, getInternalName(Buffer::class.java))
    mv.visitMethodInsn(INVOKESPECIAL, getInternalName(BufferInputStream::class.java),
        "<init>",
        "(${getDescriptor(Buffer::class.java)})V",
        false)
    mv.visitVarInsn(ASTORE, localVar(3))

    mv.visitVarInsn(ALOAD, 0) // this
    mv.visitFieldInsn(GETFIELD, proxyClassInternalName, "objectMapper",
        getDescriptor(ObjectMapper::class.java))

    // Push the JsonFactory to the stack
    mv.visitVarInsn(ALOAD, 0) // this
    mv.visitFieldInsn(GETFIELD, proxyClassInternalName, "objectMapper", getDescriptor(ObjectMapper::class.java))
    mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(ObjectMapper::class.java), "getFactory",
        getMethodDescriptor(ObjectMapper::class.java.getMethod("getFactory")), false)

    // Push the parser to the stack
    mv.visitVarInsn(ALOAD, localVar(3))
    mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(JsonFactory::class.java),
        "createParser",
        getMethodDescriptor(JsonFactory::class.java.getMethod("createParser", InputStream::class.java)),
        false)
    mv.visitVarInsn(ASTORE, localVar(4))

    // Deserialize the message body as the method's return value
    mv.visitVarInsn(ALOAD, localVar(4))
    mv.visitLdcInsn(Type.getType(method.returnType))
    mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(JsonParser::class.java),
        "readValueAs",
        getMethodDescriptor(JsonParser::class.java.getMethod("readValueAs", Class::class.java)),
        false)
    mv.visitTypeInsn(CHECKCAST, getInternalName(method.returnType))

    // Return the parsed result
    mv.visitLabel(lreturn)
    mv.visitInsn(ARETURN)

    // Catch any exception and throw it as a RuntimeException
    mv.visitLabel(lcatch)
    mv.visitFrame(F_SAME1, 0, null, 1, arrayOf(getInternalName(Exception::class.java)))
    mv.visitVarInsn(ASTORE, localVar(0))

    // Construct the RuntimeException instance and throw it
    mv.visitTypeInsn(NEW, getInternalName(RuntimeException::class.java))
    mv.visitInsn(DUP)
    mv.visitVarInsn(ALOAD, localVar(0))
    mv.visitMethodInsn(INVOKESPECIAL, getInternalName(RuntimeException::class.java),
        "<init>",
        "(Ljava/lang/Throwable;)V",
        false)
    mv.visitInsn(ATHROW)

    mv.visitLabel(lend)

    // Local variables definition

    // Parameters
    method.parameterTypes.forEachIndexed { i, clazz ->
      mv.visitLocalVariable("arg$i", getDescriptor(clazz), null, lstart, lend, i + 1)
    }

    mv.visitLocalVariable("outputStream", getDescriptor(BufferOutputStream::class.java), null,
        lstart, lcatch, localVar(0))
    mv.visitLocalVariable("generator", getDescriptor(JsonGenerator::class.java), null,
        lstart, lcatch, localVar(1))
    mv.visitLocalVariable("response", getDescriptor(Message::class.java),
        "${getDescriptor(Message::class.java)}<${getDescriptor(Buffer::class.java)}>",
        lstart, lcatch, localVar(2))
    mv.visitLocalVariable("inputStream", getDescriptor(BufferInputStream::class.java), null,
        lstart, lcatch, localVar(3))
    mv.visitLocalVariable("parser", getDescriptor(JsonParser::class.java), null,
        lstart, lcatch, localVar(4))

    mv.visitLocalVariable("e", getDescriptor(Exception::class.java), null,
        lcatch, lend, localVar(0))

    mv.visitLocalVariable("this", proxyClassDescriptor, null,
        lstart, lend, 0)

    mv.visitMaxs(3, localVar(4) + 1)

    mv.visitEnd()
  }
}