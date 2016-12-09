/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.example.caller

import co.paralleluniverse.fibers.Suspendable
import co.paralleluniverse.kotlin.fiber
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import io.duna.core.bootstrap.IgniteClusterManagerFactory
import io.duna.core.proxy.RemoteServiceProxyFactory
import io.duna.core.service.ServiceVerticleFactory
import io.duna.example.echo.EchoService
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.json.Json
import io.vertx.ext.sync.SyncVerticle

object Caller {
  @JvmStatic
  fun main(vararg args: String) {
    System.setProperty("java.util.logging.manager",
      "org.apache.logging.log4j.jul.LogManager")

    val clusterManager = IgniteClusterManagerFactory.create()

    val vertxOptions = VertxOptions()
      .setClusterManager(clusterManager)

    val echoProxyClass = RemoteServiceProxyFactory()
      .loadProxyForService(EchoService::class.java)

    Vertx.clusteredVertx(vertxOptions) {
      it.result().executeBlocking({ future: Future<Injector> ->
        val injector = Guice.createInjector(object : AbstractModule() {
          override fun configure() {
            bind(Vertx::class.java).toInstance(Vertx.currentContext().owner())
            bind(ObjectMapper::class.java).toInstance(Json.mapper)
            bind(ServiceVerticleFactory::class.java).asEagerSingleton()
          }
        })

        future.complete(injector)
      }, { injector ->
        it.result().deployVerticle(object : SyncVerticle() {
          @Suspendable
          override fun start() {
            val echoProxy = echoProxyClass.newInstance()
            injector.result().injectMembers(echoProxy)

            fiber {
              println("Got: ${echoProxy.echo("Yoleihu")}")
            }
          }
        })
      })
    }
  }
}
