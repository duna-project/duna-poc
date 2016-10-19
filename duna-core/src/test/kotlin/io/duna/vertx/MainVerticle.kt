package io.duna.vertx

import co.paralleluniverse.fibers.Suspendable
import co.paralleluniverse.kotlin.fiber
import co.paralleluniverse.strands.Strand
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.Guice
import io.duna.core.io.BufferInputStream
import io.duna.core.io.BufferOutputStream
import io.duna.core.proxy.ProxyClassLoader
import io.duna.core.proxy.ServiceProxyFactory
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
import io.vertx.ext.sync.Sync
import io.vertx.ext.sync.SyncVerticle

class MainVerticle : SyncVerticle() {

  val service = SampleServiceImpl

  @Suspendable
  override fun start() {

    println("Deploying consumer")

    vertx.eventBus().consumer<Buffer> ("address") {
      println("Replying sender")

//      val inputStream = BufferInputStream(it.body())
//      val query = Json.mapper.readerFor(Classificacao::class.java).readValue<Classificacao>(inputStream)

      val result = service.call("", null, 0.0, false, null)

      val outputStream = BufferOutputStream(1024)
      val writer = Json.mapper.writerFor(POJO::class.java)
      writer.writeValue(outputStream, result)

      it.reply(outputStream.buffer)
    }
  }
}

class OtherVerticle : SyncVerticle() {

  @Suspendable
  override fun start() {
    println("Deploying other verticle")

    val clas = Classificacao(2016,
        Autor(1),
        Emenda(1),
        Programacao(1),
        Dotacao(1),
        Beneficiario(1),
        Impedimento(1))

    println("Sending $clas")

    val injector = Guice.createInjector(object : AbstractModule() {
      override fun configure() {
        bind(Vertx::class.java).toInstance(vertx)
        bind(ObjectMapper::class.java).toInstance(Json.prettyMapper)
      }
    })

    val service: SampleService = SampleServiceProxy()

    injector.injectMembers(service)

    val result = service.call("", null, 0.0, false, null)
    println(result)
  }
}
