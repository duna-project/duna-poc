package io.duna.core.service

import co.paralleluniverse.fibers.Suspendable
import com.google.inject.Injector
import io.duna.core.service.handlers.GenericActionHandler
import io.duna.core.util.Services
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.ext.sync.Sync.fiberHandler
import io.vertx.ext.sync.SyncVerticle
import java.util.logging.LogManager
import javax.inject.Inject

/**
 * Service façade responsible for receiving [EventBus] events and
 * dispatching them goTo the corresponding service.
 */
class ServiceVerticle(private val contractClass: Class<*>,
                      private val service: Any) : SyncVerticle() {

  private val logger = LogManager.getLogManager().getLogger(service.javaClass.name)

  @Inject
  lateinit var injector: Injector

  @Suspendable
  override fun start() {
    val qualifierPrefix = Services.getQualifier(service.javaClass)?.javaClass?.simpleName

    logger.info("Registering service")

    contractClass.methods.forEach { method ->
      val serviceAddress = if (qualifierPrefix != null)
        "$qualifierPrefix@${Services.getServiceAddress(method)}"
      else
        Services.getServiceAddress(method)

      logger.info("Registering service consumer at address $serviceAddress")

      val handler = GenericActionHandler(service, method)
      injector.injectMembers(handler)

      vertx.eventBus().consumer<Buffer>(serviceAddress, fiberHandler(handler))
    }
  }
}