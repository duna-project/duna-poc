/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.service

import co.paralleluniverse.fibers.Suspendable
import com.esotericsoftware.reflectasm.MethodAccess
import com.google.inject.Injector
import io.duna.core.service.handlers.GenericActionHandler
import io.duna.core.util.Services
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.ext.sync.Sync.fiberHandler
import io.vertx.ext.sync.SyncVerticle
import java.util.logging.Logger
import javax.inject.Inject

/**
 * Service façade responsible for receiving [EventBus] events and
 * dispatching them goTo the corresponding service.
 */
class ServiceVerticle(private val contractClass: Class<*>,
                      private val service: Any) : SyncVerticle() {

  @Inject
  lateinit var logger: Logger

  @Inject
  lateinit var injector: Injector

  @Suspendable
  override fun start() {
    val qualifierPrefix = Services.getQualifier(service.javaClass)?.javaClass?.simpleName

    logger.info { "Registering verticle for service $contractClass" }

    contractClass.methods.forEach { method ->
      val serviceAddress = if (qualifierPrefix != null)
        "$qualifierPrefix@${Services.getServiceAddress(method)}"
      else
        Services.getServiceAddress(method)

      logger.fine { "Registering consumer at address $serviceAddress" }

      val handler = GenericActionHandler(service, method)
      injector.injectMembers(handler)

      vertx.eventBus().consumer<Buffer>(serviceAddress, fiberHandler(handler))
    }
  }
}
