/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.service

import co.paralleluniverse.fibers.Suspendable
import com.google.inject.assistedinject.Assisted
import io.duna.core.service.handler.DefaultServiceHandler
import io.duna.util.Services
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.ext.sync.Sync.fiberHandler
import io.vertx.ext.sync.SyncVerticle
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
                               private val handlerFactory: DefaultServiceHandler.BinderFactory)
  : SyncVerticle() {

  @Suspendable
  override fun start() {
    val qualifierPrefix = Services.getQualifier(service.javaClass)?.javaClass?.simpleName

    logger.info { "Registering verticle for service $contractClass" }

    contractClass.methods.forEach { method ->
      val serviceAddress =
        if (qualifierPrefix != null) "$qualifierPrefix@${Services.getInternalServiceAddress(method)}"
        else Services.getInternalServiceAddress(method)

      logger.fine { "Registering consumer at address $serviceAddress" }

      val handler = handlerFactory.create(service, method)
      vertx.eventBus().consumer<Buffer>(serviceAddress, fiberHandler(handler))
    }
  }

  interface BinderFactory {
    fun create(contractClass: Class<*>, service: Any): ServiceVerticle
  }
}
