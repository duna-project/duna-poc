package io.duna.core.bootstrap

import co.paralleluniverse.fibers.instrument.JavaAgent as QuasarJavaAgent
import com.ea.agentloader.AgentLoader
import com.ea.agentloader.AgentLoaderHotSpot
import com.ea.agentloader.ClassPathUtils
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Key
import com.typesafe.config.ConfigFactory
import io.duna.agent.DunaJavaAgent
import io.duna.core.classpath.ClasspathScanner
import io.duna.core.inject.LocalServiceBinderModule
import io.duna.core.inject.RemoteServiceBinderModule
import io.duna.core.service.Contract
import io.duna.core.service.ServiceVerticle
import io.duna.core.service.ServiceVerticleFactory
import io.vertx.core.*
import io.vertx.core.json.Json
import io.vertx.spi.cluster.ignite.IgniteClusterManager
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.logger.log4j2.Log4J2Logger
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager

object Bootstrap {

  @JvmStatic
  fun main(vararg args: String) {
    // Configure the JUL to Log4J bridge
    System.setProperty("java.util.logging.manager",
      "org.apache.logging.log4j.jul.LogManager")

    JavaAgentsLoader.attachRequiredJavaAgents()

    val config = ConfigFactory.load()
    val clusterManager = IgniteClusterManagerProvider.get()

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

          // TODO Support qualifiers
          res.result().deployVerticle("duna:$verticle")
        }
      })
    }
  }
}