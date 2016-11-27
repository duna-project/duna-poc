package io.duna.core.service

import co.paralleluniverse.fibers.Suspendable
import com.google.inject.Injector
import io.duna.core.service.handlers.GenericActionHandler
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.ext.sync.Sync.fiberHandler
import io.vertx.ext.sync.SyncVerticle
import org.apache.logging.log4j.LogManager
import javax.inject.Inject

/**
 * Service façade responsible to receiving [EventBus] events and
 * dispatching them to the corresponding service.
 */
class ServiceVerticle<T : Any>(private val serviceClass: Class<T>) : SyncVerticle() {

  @Inject
  lateinit var injector: Injector

  var service: T? = null

  private val logger = LogManager.getLogger(ServiceVerticle::class.java)

  @Suspendable
  override fun start() {
    logger.info("Creating verticle for service ${serviceClass.canonicalName}")

    service = injector.getBinding(serviceClass).provider.get()

    val serviceAddress = serviceClass.getAnnotation(Address::class.java)?.value ?:
        serviceClass.canonicalName ?: ""

    serviceClass.methods.forEach { method ->
      val handler = GenericActionHandler(service, method)
      injector.injectMembers(handler)

      logger.info("Registering consumer $serviceAddress.${method.name}")

      vertx.eventBus().consumer<Buffer>("$serviceAddress.${method.name}", fiberHandler(handler))
    }
  }
}