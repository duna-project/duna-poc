package io.duna.web

import co.paralleluniverse.fibers.Suspendable
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.ext.web.Router

class HttpRouterVerticle : AbstractVerticle() {

  override fun start(startFuture: Future<Void>?) {
    val httpServer = vertx.createHttpServer()

    val router = Router.router(vertx)

    // Classpath

    httpServer.requestHandler(router::accept).listen(8080)
    startFuture?.complete()
  }
}
