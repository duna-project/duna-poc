package io.duna.example

import com.google.inject.Guice
import io.duna.core.inject.ServiceBinderModule
import io.duna.core.io.BufferOutputStream
import io.duna.core.service.Address
import io.duna.core.service.Contract
import io.duna.core.service.Service
import io.duna.core.service.ServiceVerticle
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.Json
import io.vertx.spi.cluster.ignite.IgniteClusterManager
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder
import javax.inject.Inject

@Contract
@Address("a")
interface ServiceA {
  fun ping(value: String): String
}

@Contract
@Address("b")
interface ServiceB {
  fun forwardPing(value: String): String
}

@Service
class ServiceAImpl : ServiceA {
  override fun ping(value: String): String {
    return "pong"
  }
}

@Service
class ServiceBImpl : ServiceB {

  @Inject
  lateinit var serviceA: ServiceA

  override fun forwardPing(value: String): String {
    return serviceA.ping(value)
  }
}

object StartA {

  @JvmStatic
  fun main(vararg args: String) {
    val discoverySpi = TcpDiscoverySpi()

    val ipFinder = TcpDiscoveryMulticastIpFinder()

    val communicationSpi = TcpCommunicationSpi()
    communicationSpi.localPort = 5001

    val igniteConfig = IgniteConfiguration()
        .setDiscoverySpi(discoverySpi)
        .setCommunicationSpi(communicationSpi)

    val clusterManager = IgniteClusterManager(igniteConfig)

    val vertxOptions = VertxOptions()
        .setClusterManager(clusterManager)

    println("Scanning")
    val scanResult = FastClasspathScanner().scan()
    val injector = Guice.createInjector(ServiceBinderModule(scanResult))

    val vertxA = Vertx.clusteredVertx(vertxOptions) {
      val verticleA = ServiceVerticle(ServiceA::class.java)
      injector.injectMembers(verticleA)

      it.result().deployVerticle(verticleA)

      println("done A")
    }
  }
}

object StartCaller {

  @JvmStatic
  fun main(vararg args: String) {
    val discoverySpi = TcpDiscoverySpi()

    val ipFinder = TcpDiscoveryMulticastIpFinder()
    ipFinder.setAddresses(listOf("127.0.0.1:5001"))

    val communicationSpi = TcpCommunicationSpi()
    communicationSpi.localPort = 5001

    val igniteConfig = IgniteConfiguration()
        .setCommunicationSpi(communicationSpi)
        .setDiscoverySpi(discoverySpi)

    val clusterManager = IgniteClusterManager(igniteConfig)

    val vertxOptions = VertxOptions()
        .setClusterManager(clusterManager)

    println("Running caller")

    val vertxCaller = Vertx.clusteredVertx(vertxOptions) {
      val vertx = it.result()

      val request = Buffer.buffer()
      val outBuffer = BufferOutputStream(request)
      val generator = Json.prettyMapper.factory.createGenerator(outBuffer)

      generator.writeObject("asdasd")

      vertx.eventBus().send<Buffer>("a.ping", request) { res ->
        println("Response: ${res.result()}")
      }

      println("done Caller")
    }
  }
}

object StartB {

  @JvmStatic
  fun main(vararg args: String) {
    val vertxOptions = VertxOptions()

    println("Scanning")
    val scanResult = FastClasspathScanner().scan()
    val injector = Guice.createInjector(ServiceBinderModule(scanResult))

    val vertxB = Vertx.clusteredVertx(vertxOptions) {
      val verticleB = ServiceVerticle(ServiceB::class.java)
      injector.injectMembers(verticleB)

      it.result().deployVerticle(verticleB)

      println("done B")
    }
  }
}