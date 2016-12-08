/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.bootstrap

import com.typesafe.config.ConfigFactory
import io.vertx.spi.cluster.ignite.IgniteClusterManager
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.logger.log4j2.Log4J2Logger
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder
import org.apache.logging.log4j.Level

object IgniteClusterManagerProvider {

  fun create(): IgniteClusterManager {
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

    return IgniteClusterManager(igniteConfig)
  }
}
