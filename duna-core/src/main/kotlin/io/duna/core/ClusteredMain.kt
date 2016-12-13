/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core

import com.typesafe.config.ConfigFactory
import io.duna.core.bootstrap.IgniteClusterManagerFactory
import io.duna.core.bootstrap.SupervisorVerticle
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import org.apache.logging.log4j.LogManager
import co.paralleluniverse.fibers.instrument.JavaAgent as QuasarJavaAgent

object ClusteredMain {

  @JvmStatic
  fun main(vararg args: String) {
    val config = ConfigFactory.load()
    val clusterManager = IgniteClusterManagerFactory.create()

    val vertxOptions = VertxOptions()
      .setClusterManager(clusterManager)
      .setHAEnabled(config.getBoolean("duna.vertx.ha-enabled"))
      .setWorkerPoolSize(config.getInt("duna.thread-pool-size"))

    val rootLogger = LogManager.getRootLogger()

    rootLogger.info { "Starting the cluster node" }

    Vertx.clusteredVertx(vertxOptions) { res ->
      if (res.failed()) {
        rootLogger.error("Error while creating clustered vert.x instance",
          res.cause())
        return@clusteredVertx
      }

      rootLogger.debug { "Deploying the supervisor" }
      res.result().deployVerticle(SupervisorVerticle())
    }
  }
}
