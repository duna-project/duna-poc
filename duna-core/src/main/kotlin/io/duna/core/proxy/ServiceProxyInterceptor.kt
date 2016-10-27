package io.duna.core.proxy

import co.paralleluniverse.fibers.Suspendable
import io.duna.core.io.BufferInputStream
import io.duna.core.io.BufferOutputStream
import io.duna.core.service.Address
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.Message
import io.vertx.ext.sync.Sync.awaitResult
import net.bytebuddy.implementation.bind.annotation.AllArguments
import net.bytebuddy.implementation.bind.annotation.Origin
import net.bytebuddy.implementation.bind.annotation.This
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

internal class ServiceProxyInterceptor {

  val addressCache = ConcurrentHashMap<Method, String>()

  @Suppress("unused")
  @Suspendable
  fun intercept(@AllArguments arguments: Array<Any>,
                @Origin(cache = true) method: Method,
                @This proxy: ServiceProxy): Any {

    val outBuffer = BufferOutputStream(Buffer.buffer(1024))
    val generator = proxy.getObjectMapper().factory.createGenerator(outBuffer)

    generator.writeStartObject()
    arguments.forEachIndexed { i, any -> generator.writeObjectField("$i", any) }
    generator.writeEndObject()

    generator.flush()
    generator.close()

    val address = addressCache[method] ?:
        method.declaringClass.getAnnotation(Address::class.java)?.value
        ?: method.declaringClass.canonicalName

    val deliveryOptions = DeliveryOptions()
    deliveryOptions.addHeader("action", method.name)
    deliveryOptions.addHeader("paramTypes",
        method.parameterTypes
            .map { it.toGenericString() }
            .joinToString(",")
    )

    // TODO: Add some kind of filter support here

    val response = awaitResult<Message<Buffer>>({
      proxy.getVertx().eventBus().send(address, outBuffer.buffer, deliveryOptions, it)
    }, 1000).body()

    if (response.length() > 0) {
      val inBuffer = BufferInputStream(response)
      val parser = proxy.getObjectMapper().factory.createParser(inBuffer)
      val result = parser.readValueAs(method.returnType)

      parser.close()
      return result
    }

    return Unit
  }
}