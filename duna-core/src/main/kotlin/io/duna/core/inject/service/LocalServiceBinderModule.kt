/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.inject.service

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import com.google.inject.TypeLiteral
import com.google.inject.UnsafeTypeLiteral
import com.google.inject.multibindings.MapBinder
import io.duna.core.classpath.ClassPathScanner
import io.duna.core.service.LocalServices
import io.duna.util.Services
import java.lang.reflect.Modifier
import java.util.logging.LogManager

/**
 * Binds contracts to their implementations found in the classpath.
 *
 * @author [Carlos Eduardo Melo][cemelo@redime.com.br]
 */
internal object LocalServiceBinderModule : AbstractModule() {

  @JvmStatic
  private val logger = LogManager.getLogManager()
    .getLogger(LocalServiceBinderModule::class.java.name)

  override fun configure() {
    logger.info { "Configuring local services" }
    logger.fine { "Binding local service contracts" }

    val localContracts = ClassPathScanner.getLocalServices()
        .map { Class.forName(it) }

    val localServices = MapBinder
      .newMapBinder(binder(),
        object : TypeLiteral<Class<*>>() {},
        object : TypeLiteral<Any>() {},
        LocalServices::class.java)

    localContracts.forEach contractForEach@ { contractClass ->
      if (!contractClass.isInterface && !Modifier.isAbstract(contractClass.modifiers)) {
        logger.warning { "Unable to bind ${contractClass.canonicalName}. " +
          "Contracts must be either an interface or abstract class." }
        return@contractForEach
      }

      val contractTypeLiteral = UnsafeTypeLiteral(contractClass)

      ClassPathScanner
          .getImplementationsInClasspath(contractClass)
          .map { Class.forName(it) }
          .forEach serviceForEach@ { serviceClass ->
            if (serviceClass.isInterface || Modifier.isAbstract(serviceClass.modifiers)) {
              logger.warning { "Unable to bind ${serviceClass.canonicalName}. " +
                "Implementations must be concrete classes." }
              return@serviceForEach
            }

            val qualifier = Services.getQualifier(serviceClass)

            if (qualifier != null) {
              bind(contractTypeLiteral)
                  .annotatedWith(qualifier)
                  .to(serviceClass)
                  .`in`(Scopes.SINGLETON)
            } else {
              bind(contractTypeLiteral)
                  .to(serviceClass)
                  .`in`(Scopes.SINGLETON)

              localServices
                .addBinding(contractClass)
                .to(serviceClass)
                .`in`(Scopes.SINGLETON)
            }

            logger.fine { "Bound ${contractClass.canonicalName} -> ${serviceClass.canonicalName}" }
          }
    }
  }
}
