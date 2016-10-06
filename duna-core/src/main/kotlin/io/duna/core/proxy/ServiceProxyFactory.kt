package io.duna.core.proxy

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import io.duna.asm.*
import io.duna.asm.Opcodes.*
import io.duna.asm.Type.*
import io.duna.core.io.BufferOutputStream
import io.vertx.core.Vertx
import java.io.OutputStream
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method
import javax.inject.Inject

class ServiceProxyFactory {

  fun get(service: Class<*>) {
    val cw = ClassWriter(0)

    val proxyClassName = Type.getInternalName(service) + "\$Proxy"
    val proxyClassDescriptor = "L${proxyClassName};"

    var fv: FieldVisitor
    var mv: MethodVisitor
    var av0: AnnotationVisitor

    cw.visit(V1_6, ACC_PUBLIC + ACC_FINAL + ACC_SUPER,
        proxyClassName,
        null,
        Type.getInternalName(Any::class.java),
        arrayOf(getInternalName(service)))

    cw.visitInnerClass(Type.getInternalName(MethodHandles::class.java) + "\$Lookup",
        Type.getInternalName(MethodHandles::class.java),
        "Lookup",
        ACC_PUBLIC + ACC_FINAL + ACC_STATIC)

//    run {
//      fv = cw.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "INITIAL_BUFFER_SIZE",
//          getDescriptor(Int::class.java), null, null)
//      fv.visitEnd()
//    }

    run {
      fv = cw.visitField(ACC_PRIVATE, "vertx",
          getDescriptor(Vertx::class.java), null, null);

      av0 = fv.visitAnnotation(getDescriptor(Inject::class.java), true)
      av0.visitEnd()

      fv.visitEnd()
    }

    run {
      fv = cw.visitField(ACC_PRIVATE, "objectMapper",
          getDescriptor(ObjectMapper::class.java), null, null)

      av0 = fv.visitAnnotation(getDescriptor(Inject::class.java), true)
      av0.visitEnd()

      fv.visitEnd()
    }

    run {
      fv = cw.visitField(ACC_PRIVATE + ACC_FINAL, "address",
          getDescriptor(String::class.java), null, null)
      fv.visitEnd()
    }

    // Constructor
    run {
      mv = cw.visitMethod(0, "<init>",
          "(L${getDescriptor(String::class.java)})V", null, null)
      mv.visitCode()

      val l0 = Label()
      mv.visitLabel(l0)
      mv.visitVarInsn(ALOAD, 0)
      mv.visitMethodInsn(INVOKESPECIAL, getDescriptor(Any::class.java), "<init>", "()V", false)

      val l1 = Label()
      mv.visitLabel(l1)
      mv.visitVarInsn(ALOAD, 0)
      mv.visitVarInsn(ALOAD, 1)
      mv.visitFieldInsn(PUTFIELD, proxyClassName, "address", getDescriptor(String::class.java))

      val l2 = Label()
      mv.visitLabel(l2)
      mv.visitInsn(RETURN)

      val l3 = Label()
      mv.visitLabel(l3)
      mv.visitLocalVariable("this", proxyClassDescriptor, null, l0, l3, 0)
      mv.visitLocalVariable("address",
          getDescriptor(String::class.java), null, l0, l3, 1)

      mv.visitMaxs(2, 2)
      mv.visitEnd()
    }

    service.methods.forEach {
//      generateMethod(cw, it)
    }
  }

  private fun generateMethod(classWriter: ClassWriter, proxyClassName: String, method: Method) {
    // the reference to *this* is stored at index 0
    var firstNonParameterLocalVar = 1

    for (param in method.parameterTypes) {
      if (param.isAssignableFrom(Double::class.java) || param.isAssignableFrom(Long::class.java))
        firstNonParameterLocalVar += 2
      else
        firstNonParameterLocalVar++
    }

    val localVar = { i: Int -> firstNonParameterLocalVar + i }

    val mv = classWriter.visitMethod(ACC_PUBLIC, method.name,
        getMethodDescriptor(method),
        null, // signature
        null)

    mv.visitCode()

    val l0 = Label()
    val l1 = Label()
    val l2 = Label()

    mv.visitTryCatchBlock(l0, l1, l2, getDescriptor(Exception::class.java))
    mv.visitLabel(l0)

    // Creates a new instance of BufferOutputStream
    mv.visitTypeInsn(NEW, getDescriptor(BufferOutputStream::class.java))
    mv.visitInsn(DUP)

    // Invokes the constructor
    mv.visitIntInsn(SIPUSH, 1024)
    mv.visitMethodInsn(INVOKESPECIAL, getDescriptor(BufferOutputStream::class.java), "<init>", "(I)V", false)
    mv.visitVarInsn(ASTORE, localVar(0))

    // Push the JsonFactory to the stack
    mv.visitVarInsn(ALOAD, 0) // this
    mv.visitFieldInsn(GETFIELD, proxyClassName, "objectMapper", getDescriptor(ObjectMapper::class.java))
    mv.visitMethodInsn(INVOKEVIRTUAL, getDescriptor(ObjectMapper::class.java), "getFactory",
        getMethodDescriptor(ObjectMapper::class.java.getMethod("getFactory")), false)

    // Create the JsonGenerator
    mv.visitVarInsn(ALOAD, localVar(0)) // outputStream
    mv.visitMethodInsn(INVOKEVIRTUAL, getDescriptor(JsonFactory::class.java),
        "createGenerator",
        getMethodDescriptor(JsonFactory::class.java.getMethod("createGenerator", OutputStream::class.java)),
        false)
    mv.visitVarInsn(ASTORE, localVar(1))

    mv.visitVarInsn(ALOAD, localVar(1))
    mv.visitMethodInsn(INVOKEVIRTUAL, getDescriptor(JsonGenerator::class.java),
        "writeStartArray",
        "()V",
        false)

    val x = Integer(1)


    method.genericParameterTypes.forEachIndexed { i, type ->
      mv.visitVarInsn(ALOAD, localVar(1)) // stack the generator
      mv.visitVarInsn(ALOAD, i + 1) // stack the parameter
//      mv.visit
    }

    mv.visitEnd()
  }

  private fun getInternalName(clazz: Class<*>): String {
    return clazz.canonicalName.replace('.', '/')
  }
}