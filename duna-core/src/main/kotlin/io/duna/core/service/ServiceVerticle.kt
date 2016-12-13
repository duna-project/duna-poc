/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.service

import co.paralleluniverse.fibers.Suspendable
import com.google.inject.assistedinject.Assisted
import io.duna.core.service.handlers.DefaultServiceHandler
import io.duna.core.util.Services
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.ext.sync.Sync.fiberHandler
import io.vertx.ext.sync.SyncVerticle
import net.bytebuddy.description.type.TypeDescription
import java.util.logging.Logger
import javax.inject.Inject

/**
 * Service façade responsible for receiving [EventBus] events and
 * dispatching them to the corresponding service.
 */
class ServiceVerticle
  @Inject internal constructor(@Assisted private val contractClass: Class<*>,
                               @Assisted private val service: Any,
                               private val logger: Logger,
                               private val handlerFactory: DefaultServiceHandler.Factory)
  : SyncVerticle() {

  @Suspendable
  override fun start() {
    val qualifierPrefix = Services.getQualifier(service.javaClass)?.javaClass?.simpleName

    logger.info { "Registering verticle for service $contractClass" }

    contractClass.methods.forEach { method ->
      val serviceAddress =
        if (qualifierPrefix != null) "$qualifierPrefix@${Services.getServiceAddress(method)}"
        else Services.getServiceAddress(method)

      logger.fine { "Registering consumer at address $serviceAddress" }

      val handler = handlerFactory.create(service, method)
      vertx.eventBus().consumer<Buffer>(serviceAddress, fiberHandler(handler))
    }
  }

  interface Factory {
    fun create(contractClass: Class<*>, service: Any): ServiceVerticle
  }
}
