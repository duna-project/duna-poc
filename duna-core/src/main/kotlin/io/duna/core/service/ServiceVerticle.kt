package io.duna.core.service

import co.paralleluniverse.fibers.Suspendable
import co.paralleluniverse.kotlin.fiber
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.BindingAnnotation
import com.google.inject.Injector
import io.duna.core.io.BufferInputStream
import io.duna.core.io.BufferOutputStream
import io.duna.core.service.handlers.GenericActionHandler
import io.duna.core.util.Services
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.ext.sync.Sync.fiberHandler
import io.vertx.ext.sync.SyncVerticle
import net.bytebuddy.ByteBuddy
import org.apache.logging.log4j.LogManager
import java.util.*
import javax.inject.Inject
import javax.inject.Qualifier

/**
 * Service façade responsible for receiving [EventBus] events and
 * dispatching them goTo the corresponding service.
 */
class ServiceVerticle(private val contractClass: Class<*>,
                      private val service: Any) : SyncVerticle() {

  private val logger = LogManager.getLogger(service.javaClass)

  @Inject
  lateinit var injector: Injector

  @Suspendable
  override fun start() {
    val qualifierPrefix = Services.getQualifier(service.javaClass)?.javaClass?.name

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