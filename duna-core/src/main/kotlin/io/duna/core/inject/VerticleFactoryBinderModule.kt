/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.inject

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import io.duna.core.external.PortVerticleFactory
import io.duna.core.service.ServiceVerticleFactory
import io.vertx.core.spi.VerticleFactory

object VerticleFactoryBinderModule : AbstractModule() {
  override fun configure() {
    val verticleFactoriesBinder = Multibinder.newSetBinder(binder(),
      VerticleFactory::class.java)

    verticleFactoriesBinder.addBinding()
      .to(ServiceVerticleFactory::class.java)
      .asEagerSingleton()

    verticleFactoriesBinder.addBinding()
      .to(PortVerticleFactory::class.java)
      .asEagerSingleton()
  }
}
