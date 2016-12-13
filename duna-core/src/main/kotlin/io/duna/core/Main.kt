package io.duna.core

import com.typesafe.config.ConfigFactory
import io.duna.core.bootstrap.SupervisorVerticle
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import org.apache.logging.log4j.LogManager

object Main {
  @JvmStatic
  fun main(vararg args: String) {
    val config = ConfigFactory.load()

    val vertxOptions = VertxOptions()
      .setHAEnabled(false)
      .setWorkerPoolSize(config.getInt("duna.thread-pool-size"))

    val rootLogger = LogManager.getRootLogger()

    rootLogger.info("Starting the cluster node")

    val vertxInstance = Vertx.vertx(vertxOptions)

    rootLogger.debug("Deploying the supervisor")
    vertxInstance.deployVerticle(SupervisorVerticle())
  }
}
