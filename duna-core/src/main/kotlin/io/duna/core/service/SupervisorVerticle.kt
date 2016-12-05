package io.duna.core.service

import co.paralleluniverse.fibers.Suspendable
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.Guice
import io.duna.core.inject.LocalServiceBinderModule
import io.duna.core.inject.RemoteServiceBinderModule
import io.vertx.core.Verticle
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.ext.sync.SyncVerticle
import java.util.logging.LogManager

class SupervisorVerticle : SyncVerticle() {

  private val logger = LogManager.getLogManager()
    .getLogger(SupervisorVerticle::class.java.name)

  @Suspendable
  override fun start() {

    logger.fine("Configuring proxies and instances for dependency injection")

    val parentInjector = Guice.createInjector(object : AbstractModule() {
      override fun configure() {
        bind(Vertx::class.java).toInstance(vertx)
        bind(ObjectMapper::class.java).toInstance(Json.mapper)

        install(RemoteServiceBinderModule)
      }
    })

    val localInjector = parentInjector.createChildInjector(LocalServiceBinderModule)
    val verticles = mutableListOf<Verticle>()


    logger.fine("Creating verticles for all local services")
    localInjector.bindings.entries
      .filter { (key, _) -> key.typeLiteral.rawType.isAnnotationPresent(Contract::class.java) }
      .forEach { (key, value) ->
        val serviceVerticle = ServiceVerticle(key.typeLiteral.rawType,
          value.provider.get())

        localInjector.injectMembers(serviceVerticle)
        verticles += serviceVerticle
      }

    logger.fine("Deploying verticles")
    verticles.forEach { vertx.deployVerticle(it) }
  }
}