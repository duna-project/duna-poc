package io.duna.core

import com.google.inject.Guice
import com.google.inject.Stage
import io.duna.core.inject.InjectorModuleBinder
import io.duna.core.proxy.ServiceImplAndProxyBinder
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.eventbus.EventBusOptions
import io.vertx.spi.cluster.ignite.IgniteClusterManager
import org.apache.ignite.configuration.IgniteConfiguration
import java.util.concurrent.atomic.AtomicInteger

object Bootstrap {

  val deployedInstances: AtomicInteger = AtomicInteger(0)

  @JvmStatic
  fun main(args: Array<String>) {
    val scanResult = FastClasspathScanner()
      .scan(Runtime.getRuntime().availableProcessors())

    val injector = Guice.createInjector(Stage.DEVELOPMENT,
        ServiceImplAndProxyBinder(scanResult),
        InjectorModuleBinder(scanResult))

    val clusterManager = IgniteClusterManager()

    val eventBusOptions = EventBusOptions()

    val options = VertxOptions()
      .setClusterManager(clusterManager)
      .setEventBusOptions(eventBusOptions)

    Vertx.clusteredVertx(options) { res ->
      val mainVerticle = MainVerticle()
      injector.injectMembers(mainVerticle)

      val deploymentOptions = DeploymentOptions()
        .setInstances(Runtime.getRuntime().availableProcessors())

      res.result().deployVerticle(mainVerticle)
    }
  }
}