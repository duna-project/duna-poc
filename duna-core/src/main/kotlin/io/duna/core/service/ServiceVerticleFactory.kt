package io.duna.core.service

import com.google.inject.Injector
import com.google.inject.Key
import io.vertx.core.Verticle
import io.vertx.core.spi.VerticleFactory
import java.util.logging.Logger
import javax.inject.Inject

class ServiceVerticleFactory : VerticleFactory {

  @Inject
  lateinit var injector: Injector

  @Inject
  lateinit var logger: Logger

  override fun createVerticle(verticleName: String, classLoader: ClassLoader): Verticle {
    val contractClass = classLoader.loadClass(VerticleFactory.removePrefix(verticleName).substringBefore("@"))
    val qualifierName = VerticleFactory.removePrefix(verticleName).substringAfter("@", "")

    logger.fine { "Creating verticle for service $verticleName" }

    val implementation = if (qualifierName.isNotBlank()) {
      val qualifierClass = classLoader.loadClass(qualifierName)

      if (qualifierClass.isAnnotation) {
        @Suppress("UNCHECKED_CAST")
        injector.getBinding(Key.get(contractClass, qualifierClass as Class<Annotation>)).provider.get()
      } else {
        // Error
        throw IllegalStateException()
      }
    } else {
      injector.getBinding(contractClass).provider.get()
    }

    val verticleInstance = ServiceVerticle(contractClass, implementation)
    injector.injectMembers(verticleInstance)

    return verticleInstance
  }

  override fun prefix() = "duna"
}