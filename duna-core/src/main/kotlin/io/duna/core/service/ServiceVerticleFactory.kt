/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.service

import com.google.inject.Injector
import com.google.inject.Key
import io.vertx.core.Verticle
import io.vertx.core.Vertx
import io.vertx.core.spi.VerticleFactory
import java.util.logging.Logger
import javax.inject.Inject

class ServiceVerticleFactory
  @Inject constructor(private val injector: Injector,
                      private val verticleFactory: ServiceVerticle.BinderFactory,
                      private val logger: Logger)
  : VerticleFactory {

  override fun createVerticle(verticleName: String, classLoader: ClassLoader): Verticle {
    val contractClass = classLoader.loadClass(VerticleFactory.removePrefix(verticleName)
      .substringBefore("@"))
    val qualifierName = VerticleFactory.removePrefix(verticleName).substringAfter("@", "")

    logger.fine { "Creating verticle for service $verticleName" }

    val implementation = if (qualifierName.isNotBlank()) {
      val qualifierClass = classLoader.loadClass(qualifierName)

      if (qualifierClass.isAnnotation) {
        @Suppress("UNCHECKED_CAST")
        injector.getBinding(Key.get(contractClass, qualifierClass as Class<Annotation>)).provider.get()
      } else {
        // Error
        throw IllegalStateException("The qualifier provided isn't an annotation.")
      }
    } else {
      injector.getBinding(contractClass).provider.get()
    }

    val verticleInstance = verticleFactory.create(contractClass, implementation)

    return verticleInstance
  }

  override fun blockingCreate(): Boolean = true

  override fun order(): Int = 2

  override fun prefix() = "duna"
}
