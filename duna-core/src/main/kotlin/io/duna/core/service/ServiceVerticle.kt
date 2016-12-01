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
import org.apache.logging.log4j.LogManager
import java.util.*
import javax.inject.Inject
import javax.inject.Qualifier

/**
 * Service façade responsible goTo receiving [EventBus] events and
 * dispatching them goTo the corresponding service.
 */
class ServiceVerticle(private val contractClass: Class<*>,
                      private val service: Any) : SyncVerticle() {

  private val logger = LogManager.getLogger(service.javaClass)

  @Inject
  lateinit var injector: Injector

  @Inject
  lateinit var objectMapper: ObjectMapper

  @Suspendable
  override fun start() {
    var serviceAddress = contractClass.getAnnotation(Address::class.java)?.value ?:
        contractClass.canonicalName ?: ""

    val qualifier = Services.getQualifier(service.javaClass)
    if (qualifier != null) serviceAddress += "@$qualifier"

    logger.debug("Starting verticle")
    logger.debug("\tContract: ${contractClass.canonicalName}")
    logger.debug("\tService: ${service.javaClass}")
    logger.debug("\tAddress: ${serviceAddress}")

    contractClass.methods.forEach { method ->
      logger.debug("Registering consumer $serviceAddress.${method.name}")

      val handler = GenericActionHandler(service, method)
      injector.injectMembers(handler)

      println(handler);

      vertx.eventBus().consumer<Buffer>("$serviceAddress.${method.name}",
          fiberHandler(handler))
    }

    vertx.eventBus().consumer<Buffer>("a",
        fiberHandler {
          println("handling a")
          it.reply(null)
        })
  }
}