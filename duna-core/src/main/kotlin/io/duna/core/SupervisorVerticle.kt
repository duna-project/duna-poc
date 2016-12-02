package io.duna.core

import co.paralleluniverse.fibers.Suspendable
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.Guice
import io.duna.core.inject.AnnotateRemoteContractsAsSuspendableModule
import io.duna.core.inject.LocalServiceBinderModule
import io.duna.core.inject.RemoteServiceBinderModule
import io.duna.core.service.Contract
import io.duna.core.service.ServiceVerticle
import io.vertx.core.Verticle
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.ext.sync.SyncVerticle
import net.bytebuddy.ByteBuddy
import org.apache.logging.log4j.LogManager

class SupervisorVerticle : SyncVerticle() {

  private val logger = LogManager.getLogger(SupervisorVerticle::class.java)

  @Suspendable
  override fun start() {

    logger.debug("Configuring proxies and instances for dependency injection")

    val parentInjector = Guice.createInjector(object : AbstractModule() {
      override fun configure() {
        bind(Vertx::class.java).toInstance(vertx)
        bind(ObjectMapper::class.java).toInstance(Json.mapper)

        install(AnnotateRemoteContractsAsSuspendableModule())
        install(RemoteServiceBinderModule())
      }
    })

    val localInjector = parentInjector.createChildInjector(LocalServiceBinderModule())
    val verticles = mutableListOf<Verticle>()


    logger.debug("Creating verticles for all local services")
    localInjector.bindings.entries
      .filter { (key, _) -> key.typeLiteral.rawType.isAnnotationPresent(Contract::class.java) }
      .forEach { (key, value) ->
        val serviceVerticle = ServiceVerticle(key.typeLiteral.rawType,
          value.provider.get())

        localInjector.injectMembers(serviceVerticle)
        verticles += serviceVerticle
      }

    logger.debug("Deploying verticles")
    verticles.forEach { vertx.deployVerticle(it) }
  }
}