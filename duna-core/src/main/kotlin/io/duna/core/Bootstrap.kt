package io.duna.core

import com.typesafe.config.ConfigFactory
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.spi.cluster.ignite.IgniteClusterManager
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder

object Bootstrap {

  @JvmStatic
  fun main(vararg args: String) {
    val config = ConfigFactory.load()

    val discoverySpi = TcpDiscoverySpi()

    val ipFinder = TcpDiscoveryMulticastIpFinder()
    ipFinder.setAddresses(config.getStringList("duna.cluster.hosts"))

    val communicationSpi = TcpCommunicationSpi()
    communicationSpi.localPort = config.getInt("duna.cluster.local-port")

    val igniteConfig = IgniteConfiguration()
        .setDiscoverySpi(discoverySpi)
        .setCommunicationSpi(communicationSpi)

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