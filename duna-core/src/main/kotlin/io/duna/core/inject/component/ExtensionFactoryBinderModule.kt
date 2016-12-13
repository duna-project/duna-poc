/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.inject.component

import com.google.inject.AbstractModule
import com.google.inject.assistedinject.FactoryModuleBuilder
import com.google.inject.multibindings.Multibinder
import io.duna.port.Port
import io.duna.core.classpath.ClassPathScanner
import io.duna.core.service.ServiceVerticle
import io.duna.core.service.handler.DefaultServiceHandler

object ExtensionFactoryBinderModule : AbstractModule() {
  override fun configure() {
    val portClassNames = Multibinder.newSetBinder(binder(),
      String::class.java, Port::class.java)

    ClassPathScanner.getPortExtensions().forEach {
      portClassNames.addBinding().toInstance(it)
    }

    // ServiceVerticle binder factory
    install(FactoryModuleBuilder()
      .implement(ServiceVerticle::class.java, ServiceVerticle::class.java)
      .build(ServiceVerticle.BinderFactory::class.java))

    // Handler binder factory
    install(FactoryModuleBuilder()
      .implement(DefaultServiceHandler::class.java, DefaultServiceHandler::class.java)
      .build(DefaultServiceHandler.BinderFactory::class.java))
  }
}
