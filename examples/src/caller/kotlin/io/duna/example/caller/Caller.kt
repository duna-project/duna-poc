/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.example.caller

import co.paralleluniverse.fibers.Suspendable
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import io.duna.cluster.HazelcastClusterManagerFactory
import io.duna.core.inject.VerticleBinderModule
import io.duna.core.proxy.ServiceProxyFactory
import io.duna.core.service.ServiceVerticleFactory
import io.duna.example.echo.EchoService
import io.duna.example.echo.QualifiedService
import io.netty.channel.DefaultChannelId
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

    DefaultChannelId.newInstance()

    val clusterManager = HazelcastClusterManagerFactory.create()

    val vertxOptions = VertxOptions()
      .setClusterManager(clusterManager)

    val echoProxyClass = ServiceProxyFactory.loadForContract(EchoService::class.java,
      QualifiedService::class.java)

    Vertx.clusteredVertx(vertxOptions) {
      it.result().executeBlocking({ future: Future<Injector> ->
        val injector = Guice.createInjector(object : AbstractModule() {
          override fun configure() {
            bind(Vertx::class.java).toInstance(Vertx.currentContext().owner())
            bind(ObjectMapper::class.java).toInstance(Json.mapper)
            bind(ServiceVerticleFactory::class.java).asEagerSingleton()

            bind(EchoService::class.java)
              .toInstance(echoProxyClass.newInstance() as EchoService)

            install(VerticleBinderModule())
          }
        })

        future.complete(injector)
      }, { injector ->
        if (injector.failed()) {
          injector.cause().printStackTrace()
          return@executeBlocking
        }

        it.result().deployVerticle(object : SyncVerticle() {
          @Suspendable
          override fun start() {
            val echoProxy = injector.result().getInstance(EchoService::class.java)

            val result = echoProxy.echo("Yoleihu")
            println("Got $result")
          }
        })
      })
    }
  }
}
