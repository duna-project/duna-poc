package io.duna.vertx

import co.paralleluniverse.fibers.FiberForkJoinScheduler
import co.paralleluniverse.kotlin.fiber
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions

object Test {
  @JvmStatic
  fun main(vararg args: String) {
    val vertxOptions = VertxOptions()
      .setEventLoopPoolSize(1)

    val vertx = Vertx.vertx(vertxOptions)

    vertx.deployVerticle(MainVerticle())
    vertx.deployVerticle(OtherVerticle())
  }
}