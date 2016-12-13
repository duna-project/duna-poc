/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.inject

import com.google.inject.AbstractModule
import com.google.inject.assistedinject.FactoryModuleBuilder
import com.google.inject.multibindings.Multibinder
import io.duna.core.classpath.ClassPathScanner
import io.duna.core.external.Port
import io.duna.core.service.ServiceVerticle
import io.duna.core.service.handlers.DefaultServiceHandler

object ComponentFactoryBinderModule : AbstractModule() {
  override fun configure() {
    val portExtensions = Multibinder.newSetBinder(binder(),
      String::class.java, Port::class.java)

    ClassPathScanner.getPortExtensions().forEach {
      portExtensions.addBinding()
        .toInstance("duna-port:$it")
    }

    // ServiceVerticle
    install(FactoryModuleBuilder()
      .implement(ServiceVerticle::class.java, ServiceVerticle::class.java)
      .build(ServiceVerticle.Factory::class.java))

    // Handler
    install(FactoryModuleBuilder()
      .implement(DefaultServiceHandler::class.java, DefaultServiceHandler::class.java)
      .build(DefaultServiceHandler.Factory::class.java))
  }
}
