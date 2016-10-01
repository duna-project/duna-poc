package io.duna.core.service

import net.bytebuddy.jar.asm.ClassWriter
import net.bytebuddy.jar.asm.Label
import net.bytebuddy.jar.asm.Opcodes
import net.bytebuddy.jar.asm.Opcodes.*
import java.lang.reflect.Method

/**
 * Created by eduribeiro on 29/09/2016.
 */
object ServiceHandlerFactory {

  fun createClass(method: Method) {
    if (method.parameterCount > 1)
      throw Exception("Messages can only handle one object.")

    val serviceClassName = method.declaringClass.name.replace('.', '/')
    val methodName = method.name
    val handlerClassName = serviceClassName + "$" + methodName

    val parameterType = method.parameters[0]?.parameterizedType?.typeName
        ?.replace('.', '/')?.replace(">", ";>") ?: "java/lang/Void"
    val returnType = method.genericReturnType?.typeName
        ?.replace('.', '/')?.replace(">", ";>") ?: "java/lang/Void"

    val cw = ClassWriter(0)

    cw.visit(52, ACC_SUPER, handlerClassName,
        "Ljava/lang/Object;Lio/vertx/core/Handler<Lio/vertx/core/eventbus/Message<${parameterType}>;>;",
        "java/lang/Object",
        arrayOf("io/vertx/core/Handler"))

    // Service instance
    val fv = cw.visitField(ACC_PRIVATE, "service", "L${serviceClassName};", null, null)
    fv.visitAnnotation("Lcom/google/inject/Inject;", true).visitEnd()
    fv.visitEnd()

    // Constructor
    run {
      val initVisitor = cw.visitMethod(0, "<init>", "()V", null, null)
      initVisitor.visitCode()
      initVisitor.visitVarInsn(ALOAD, 0)
      initVisitor.visitMethodInsn(INVOKESPECIAL,
          "java/lang/Object",
          "<init>",
          "()V",
          false)
      initVisitor.visitInsn(RETURN)
      initVisitor.visitMaxs(1, 1)
      initVisitor.visitEnd()
    }

    // Handle method
    run {
      val mv = cw.visitMethod(ACC_PUBLIC,
          "handle",
          "(Lio/vertx/core/eventbus/Message;)V",
          "(Lio/vertx/core/eventbus/Message<${0}>;)V",
          null)

      mv.visitAnnotation("Lco/paralleluniverse/fibers/Suspendable;", true)
        .visitEnd()

      mv.visitCode()

      val l0 = Label()
      mv.visitLabel(l0)
      mv.visitLineNumber(15, l0)
      mv.visitVarInsn(ALOAD, 1)
      mv.visitVarInsn(ALOAD, 0)
      mv.visitFieldInsn(GETFIELD, handlerClassName, "service", "L${serviceClassName};")
      mv.visitVarInsn(ALOAD, 1)
      mv.visitMethodInsn(INVOKEINTERFACE,
          "io/vertx/core/eventbus/Message", "body", "()Ljava/lang/Object;", true)
      mv.visitTypeInsn(CHECKCAST, "java/lang/String")
      mv.visitMethodInsn(INVOKEINTERFACE,
          "io/duna/core/SampleService", "ping", "(Ljava/lang/String;)Ljava/lang/String;", true)
      mv.visitMethodInsn(INVOKEINTERFACE,
          "io/vertx/core/eventbus/Message", "reply", "(Ljava/lang/Object;)V", true)
    }
  }
}