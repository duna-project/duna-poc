package io.duna.core.proxy_gen

import co.paralleluniverse.kotlin.fiber
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import io.duna.core.io.BufferOutputStream
import io.duna.core.proxy.ProxyClassLoader
import io.duna.core.service.Address
import io.duna.core.service.Contract
import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.Json
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ProxyClassLoaderTest {

  lateinit var classLoader: ProxyClassLoader
  lateinit var vertx: Vertx
  lateinit var injector: Injector

  @BeforeEach
  fun setUp() {
    classLoader = ProxyClassLoader(ClassLoader.getSystemClassLoader(),
        ServiceProxyFactory())

    vertx = Vertx.vertx()

    injector = Guice.createInjector(object : AbstractModule() {
      override fun configure() {
        bind(Vertx::class.java).toInstance(vertx)
        bind(ObjectMapper::class.java).toInstance(Json.mapper)
      }
    })
  }

  @Test
  fun `service remote proxy generation`() {
    vertx.deployVerticle(object : AbstractVerticle() {
      override fun start() {
        vertx.eventBus().consumer<Void> ("test") {
          val out = BufferOutputStream(Buffer.buffer())

          val gen = Json.mapper.factory.createGenerator(out)
          gen.writeObject("result")

          it.reply(out.buffer)
        }
      }
    })

    val remoteProxyClass = classLoader.loadProxyForService(TestService::class.java)
    val proxyInstance = remoteProxyClass.newInstance()

    injector.injectMembers(proxyInstance)

    val result = fiber { proxyInstance.someMethod() }.get()

    assertTrue { result == "result" }
  }

}

@Address("test")
@Contract
internal interface TestService {
  fun someMethod(): String
}