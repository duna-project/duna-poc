package io.duna.core.bootstrap

import com.typesafe.config.ConfigFactory
import io.duna.core.service.SupervisorVerticle
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.spi.cluster.ignite.IgniteClusterManager
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.logger.log4j2.Log4J2Logger
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Logger

object Bootstrap {

  @JvmStatic
  private val LOGGER = LogManager.getLogger(Bootstrap::class.java.name) as Logger

  @JvmStatic
  fun main(vararg args: String) {
    // Configure the JUL -> Log4J bridge
    System.setProperty("java.util.logging.manager",
      "org.apache.logging.log4j.jul.LogManager")

    val config = ConfigFactory.load()

    val discoverySpi = TcpDiscoverySpi()

    val ipFinder = TcpDiscoveryMulticastIpFinder()
    ipFinder.setAddresses(config.getStringList("duna.cluster.hosts"))

    val communicationSpi = TcpCommunicationSpi()
    communicationSpi.localPort = config.getInt("duna.cluster.local-port")

    val igniteLogger = Log4J2Logger(javaClass.classLoader.getResource("log4j2.xml"))
    igniteLogger.setLevel(Level.OFF)

    val igniteConfig = IgniteConfiguration()
        .setDiscoverySpi(discoverySpi)
        .setCommunicationSpi(communicationSpi)
      .setGridLogger(igniteLogger)

    val clusterManager = IgniteClusterManager(igniteConfig)

    val vertxOptions = VertxOptions()
        .setClusterManager(clusterManager)
        .setHAEnabled(config.getBoolean("duna.vertx.ha-enabled"))
        .setWorkerPoolSize(config.getInt("duna.thread-pool-size"))

    Vertx.clusteredVertx(vertxOptions) { res ->
      if (res.failed()) {
        println("Failed " + res.cause())
        return@clusteredVertx
      }

      println("Deploying the supervisor")
      res.result().deployVerticle(SupervisorVerticle())
    }
  }
}