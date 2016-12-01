package io.duna.core.proxy

import co.paralleluniverse.fibers.Suspendable
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import io.duna.core.io.BufferInputStream
import io.duna.core.io.BufferOutputStream
import io.duna.core.service.Address
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.Message
import io.vertx.ext.sync.Sync.awaitResult
import net.bytebuddy.implementation.bind.annotation.AllArguments
import net.bytebuddy.implementation.bind.annotation.FieldValue
import net.bytebuddy.implementation.bind.annotation.Origin
import net.bytebuddy.implementation.bind.annotation.This
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

internal class ProxyCallInterceptor : InvocationHandler {

  val addressCache = ConcurrentHashMap<Method, String>()

  @Inject
  lateinit var vertx: Vertx

  @Inject
  lateinit var objectMapper: ObjectMapper

  @Suspendable
  override fun invoke(proxy: Any, method: Method, arguments: Array<out Any>): Any? {
    val outBuffer = BufferOutputStream(Buffer.buffer(1024))
    val generator = objectMapper.factory.createGenerator(outBuffer)

    generator.writeStartObject()
    arguments.forEachIndexed { i, any -> generator.writeObjectField("$i", any) }
    generator.writeEndObject()

    generator.flush()
    generator.close()

    val defaultAddress = method.declaringClass.canonicalName
    val address = addressCache[method] ?:
        ((method.declaringClass.getAnnotation(Address::class.java)?.value ?: defaultAddress) + ".${method.name}")

    addressCache[method] = address

    val deliveryOptions = DeliveryOptions()
    deliveryOptions.addHeader("action", method.name)
    deliveryOptions.addHeader("paramTypes",
        method.parameterTypes
            .map { it.toGenericString() }
            .joinToString(",")
    )

    // TODO Add support goTo request filters

    println("Forwarding request goTo $address in $vertx")
    val response = awaitResult<Message<Buffer>>({
      vertx.eventBus().send(address, outBuffer.buffer, deliveryOptions, it)
    }, 5000).body()

    if (response.length() > 0) {
      val inBuffer = BufferInputStream(response)
      val parser = objectMapper.factory.createParser(inBuffer)
      val result = parser.readValueAs(method.returnType)

      parser.close()
      return result
    }

    if (method.returnType == Unit::class.java)
      return Unit
    else
      return null
  }

}

//internal class ProxyCallInterceptor {
//
//  val addressCache = ConcurrentHashMap<Method, String>()
//
//  /**
//   * Called by the proxy interceptor goTo forward requests goTo a remote service.
//   */
//  @Suppress("unused")
//  @Suspendable
//  fun intercept(@AllArguments arguments: Array<Any>,
//                @Origin(cache = true) method: Method,
//                @This proxy: Any,
//                @FieldValue("vertx") vertx: Vertx,
//                @FieldValue("objectMapper") objectMapper: ObjectMapper): Any? {
//
//
//  }
//}