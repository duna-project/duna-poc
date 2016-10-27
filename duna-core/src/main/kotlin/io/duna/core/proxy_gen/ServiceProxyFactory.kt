package io.duna.core.proxy_gen

import com.fasterxml.jackson.databind.ObjectMapper
import io.duna.asm.ClassVisitor
import io.duna.asm.ClassWriter
import io.duna.asm.Opcodes.*
import io.duna.asm.Type
import io.duna.asm.Type.getDescriptor
import io.duna.asm.Type.getInternalName
import io.duna.asm.commons.Method
import io.duna.core.proxy_gen.internal.DefaultServiceProxyNamingStrategy
import io.duna.core.proxy_gen.internal.ProxyMethodBytecodeGenerator
import io.duna.core.service.Address
import io.vertx.core.Vertx
import java.lang.invoke.MethodHandles
import javax.inject.Inject

/**
 * A factory responsible for creating proxies for remote microservices.
 *
 * @author Carlos Eduardo Melo <[ceduardo.melo@redime.com.br]>
 */
class ServiceProxyFactory(val namingStrategy: ServiceProxyNamingStrategy) {

  constructor() : this(DefaultServiceProxyNamingStrategy())

  /**
   * Generates a binary JVM class proxying methods from the remote instance of a service.
   *
   * @param serviceClass the class of the service to be proxied.
   * @return the binary representation of the service proxy_gen class.
   */
  fun generateFor(serviceClass: Class<*>): ByteArray {
    val cw = ClassWriter(ClassWriter.COMPUTE_MAXS)

    val cv: ClassVisitor = cw

    val classInternalName = Type.getInternalName(serviceClass) + "\$RemoteProxy"
    val remoteAddress = serviceClass.getAnnotation(Address::class.java)?.value
        ?: serviceClass.canonicalName

    cv.visit(V1_8, ACC_PUBLIC + ACC_FINAL + ACC_SUPER,
        classInternalName, null,
        getInternalName(Any::class.javaObjectType),
        arrayOf(getInternalName(serviceClass)))

    cv.visitInnerClass(getInternalName(MethodHandles::class.java) + "\$Lookup",
        getInternalName(MethodHandles::class.java),
        "Lookup",
        ACC_PUBLIC + ACC_FINAL + ACC_STATIC)

    run {
      // Vertx instance field

      val fv = cv.visitField(ACC_PRIVATE, "vertx",
          getDescriptor(Vertx::class.java),
          getDescriptor(Vertx::class.java), null)

      val av0 = fv.visitAnnotation(getDescriptor(Inject::class.java), true)
      av0.visitEnd()

      fv.visitEnd()
    }

    run {
      // ObjectMapper instance field

      val fv = cv.visitField(ACC_PRIVATE, "objectMapper",
          getDescriptor(ObjectMapper::class.java),
          getDescriptor(ObjectMapper::class.java),
          null)

      val av0 = fv.visitAnnotation(getDescriptor(Inject::class.java), true)
      av0.visitEnd()

      fv.visitEnd()
    }

    ProxyMethodBytecodeGenerator.generateDefaultConstructor(cv)

    serviceClass.declaredMethods.forEach {
      ProxyMethodBytecodeGenerator.generateMethod(Method.getMethod(it), cw, classInternalName, remoteAddress)
    }

    cv.visitEnd()

    return cw.toByteArray()
  }
}