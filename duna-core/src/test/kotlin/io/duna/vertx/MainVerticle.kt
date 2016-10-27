package io.duna.vertx

import co.paralleluniverse.fibers.Fiber
import co.paralleluniverse.fibers.Suspendable
import co.paralleluniverse.kotlin.fiber
import co.paralleluniverse.strands.Strand
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.Guice
import io.duna.core.io.BufferInputStream
import io.duna.core.io.BufferOutputStream
import io.duna.core.proxy.ProxyClassLoader
import io.duna.core.proxy_gen.ServiceProxyFactory
import io.duna.sandbox.POJO
import io.duna.sandbox.SampleService
import io.duna.sandbox.SampleServiceImpl
import io.duna.sandbox.SampleServiceProxy
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.Message
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.sync.Sync
import io.vertx.ext.sync.Sync.*
import io.vertx.ext.sync.SyncVerticle
import java.util.concurrent.atomic.AtomicInteger

class MainVerticle : SyncVerticle() {

  val service = SampleServiceImpl

  companion object {
    @JvmStatic
    val atomic = AtomicInteger(1)

    @JvmStatic
    val local = object : ThreadLocal<Int>() {

      override fun initialValue(): Int {
        return (Math.random() * 50).toInt()
      }
    }
  }

  @Suspendable
  override fun start() {
    Vertx.currentContext().put("user", local)

    vertx.eventBus().consumer<JsonObject> ("address",
      fiberHandler @Suspendable {
        val id = it.body().getInteger("request")
        val ctx = local.get()
        local.set(ctx + 1)

        println("req ${id}, context: ${ctx}, ${Vertx.currentContext()}, ${Strand.currentStrand().id}")

        Fiber.sleep(200)

        println("res ${id}, context: $ctx " + (Vertx.currentContext().get<ThreadLocal<Int>>("user")).get()
            + ", ${Vertx.currentContext()}, ${Strand.currentStrand().id}")

        it.reply(JsonObject().put("result", "bye"))
    })
  }
}

class OtherVerticle : SyncVerticle() {

  @Suspendable
  override fun start() {
    println("Other")
    for (i in 0..5) {
      vertx.eventBus().send<JsonObject>("address", JsonObject().put("request", i)) {
        // println("Response: ${it.result()}, context: ${Vertx.currentContext()}")
      }

      Strand.sleep(100)
    }
  }
}
