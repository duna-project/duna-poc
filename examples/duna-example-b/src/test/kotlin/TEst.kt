import co.paralleluniverse.fibers.Suspendable
import co.paralleluniverse.fibers.instrument.QuasarInstrumentor
import co.paralleluniverse.kotlin.fiber
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.Guice
import io.duna.core.io.BufferInputStream
import io.duna.core.io.BufferOutputStream
import io.duna.core.proxy.RemoteServiceProxyFactory
import io.vertx.core.*
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.Json
import io.vertx.ext.sync.SyncVerticle
import io.vertx.spi.cluster.ignite.IgniteClusterManager
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder
import java.lang.reflect.Field

object StartCaller {

  @JvmStatic
  fun main(vararg args: String) {
    val discoverySpi = TcpDiscoverySpi()

    val ipFinder = TcpDiscoveryMulticastIpFinder()
    ipFinder.setAddresses(listOf("127.0.0.1:5001"))

    val communicationSpi = TcpCommunicationSpi()
    communicationSpi.localPort = 5003

    val igniteConfig = IgniteConfiguration()
      .setCommunicationSpi(communicationSpi)
      .setDiscoverySpi(discoverySpi)

    val clusterManager = IgniteClusterManager(igniteConfig)

    val vertxOptions = VertxOptions()
      .setClusterManager(clusterManager)
      .setHAEnabled(true)

    val vertxCaller = Vertx.clusteredVertx(vertxOptions) {
      it.result().deployVerticle(ReceivingVerticle())
    }
  }
}

object StartReceiver {

  @JvmStatic
  fun main(vararg args: String) {
    val discoverySpi = TcpDiscoverySpi()

    val ipFinder = TcpDiscoveryMulticastIpFinder()
    ipFinder.setAddresses(listOf("127.0.0.1:5001"))

    val communicationSpi = TcpCommunicationSpi()
    communicationSpi.localPort = 5003

    val igniteConfig = IgniteConfiguration()
      .setCommunicationSpi(communicationSpi)
      .setDiscoverySpi(discoverySpi)

    val clusterManager = IgniteClusterManager(igniteConfig)

    val vertxOptions = VertxOptions()
      .setClusterManager(clusterManager)
      .setHAEnabled(true)

    val vertxCaller = Vertx.clusteredVertx(vertxOptions) {
      it.result().deployVerticle(SendingVerticle())
    }
  }
}