/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import com.typesafe.config.ConfigFactory
import io.duna.core.bootstrap.IgniteClusterManagerProvider
import io.duna.core.classpath.ClasspathScanner
import io.duna.core.inject.LocalServiceBinderModule
import io.duna.core.inject.RemoteServiceBinderModule
import io.duna.core.service.ServiceVerticleFactory
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.json.Json
import org.apache.logging.log4j.LogManager
import co.paralleluniverse.fibers.instrument.JavaAgent as QuasarJavaAgent

object Main {

  @JvmStatic
  fun main(vararg args: String) {
    // Configure the JUL to Log4J bridge
//    System.setProperty("java.util.logging.manager",
//      "org.apache.logging.log4j.jul.LogManager")

//    JavaAgentsLoader.attachRequiredJavaAgents()

    val config = ConfigFactory.load()
    val clusterManager = IgniteClusterManagerProvider.create()

    val vertxOptions = VertxOptions()
      .setClusterManager(clusterManager)
      .setHAEnabled(config.getBoolean("duna.vertx.ha-enabled"))
      .setWorkerPoolSize(config.getInt("duna.thread-pool-size"))

    val rootLogger = LogManager.getRootLogger()

    rootLogger.info { "Starting the cluster node" }

    Vertx.clusteredVertx(vertxOptions) { res ->
      if (res.failed()) {
        return@clusteredVertx
      }

      res.result().executeBlocking({ future: Future<Injector> ->
        rootLogger.debug { "Creating the dependency injector" }

        val injector = Guice.createInjector(object : AbstractModule() {
          override fun configure() {
            bind(Vertx::class.java).toInstance(Vertx.currentContext().owner())
            bind(ObjectMapper::class.java).toInstance(Json.mapper)
            bind(ServiceVerticleFactory::class.java).asEagerSingleton()

            install(RemoteServiceBinderModule)
            install(LocalServiceBinderModule)
          }
        })

        future.complete(injector)
      }, {
        rootLogger.debug { "Registering the verticle factory" }

        res.result().registerVerticleFactory(it.result()
          .getProvider(ServiceVerticleFactory::class.java).get())

        ClasspathScanner.getLocalServices().forEach { verticle ->
          rootLogger.info { "Deploying verticle duna:$verticle" }
          res.result().deployVerticle("duna:$verticle")
        }
      })
    }
  }
}
