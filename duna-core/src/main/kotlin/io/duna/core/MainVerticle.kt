package io.duna.core

import com.google.inject.Guice
import com.google.inject.Stage
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Verticle
import javax.inject.Inject

class MainVerticle : AbstractVerticle() {

  @Inject
  lateinit var verticles: Set<Verticle>

  override fun start(startFuture: Future<Void>) {
    // Create injector and register modules
    val injector = Guice.createInjector(Stage.DEVELOPMENT)
    injector.injectMembers(this)

    // Deploy registered verticles
    verticles.forEach { vertx.deployVerticle(it) }

    startFuture.complete()
  }
}