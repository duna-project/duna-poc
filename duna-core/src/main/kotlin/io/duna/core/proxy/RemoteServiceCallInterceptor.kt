package io.duna.core.proxy

import co.paralleluniverse.fibers.SuspendExecution
import com.fasterxml.jackson.databind.ObjectMapper
import io.duna.core.io.BufferInputStream
import io.duna.core.io.BufferOutputStream
import io.duna.core.util.Services
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.Message
import io.vertx.ext.sync.Sync
import net.bytebuddy.implementation.bind.annotation.AllArguments
import net.bytebuddy.implementation.bind.annotation.FieldValue
import net.bytebuddy.implementation.bind.annotation.Origin
import net.bytebuddy.implementation.bind.annotation.RuntimeType
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * Interceptor used to forward service calls to remote instances.
 *
 * @author Carlos Eduardo Melo <[ceduardo.melo@gmail.com]>
 */
internal object RemoteServiceCallInterceptor {

  val addressCache = ConcurrentHashMap<Method, String>()

  @RuntimeType
  @Throws(SuspendExecution::class, InterruptedException::class)
  fun intercept(@FieldValue("vertx") vertx: Vertx,
                @FieldValue("objectMapper") objectMapper: ObjectMapper,
                @Origin(cache = true) method: Method,
                @AllArguments vararg arguments: Any?): Any? {

    val outBuffer = BufferOutputStream(Buffer.buffer(1024))
    val generator = objectMapper.factory.createGenerator(outBuffer)

    generator.writeStartObject()
    arguments.forEachIndexed { i, any -> generator.writeObjectField("$i", any) }
    generator.writeEndObject()

    generator.flush()
    generator.close()

    val serviceAddress = addressCache[method] ?:
      Services.getServiceAddress(method)

    addressCache[method] = serviceAddress

    val deliveryOptions = DeliveryOptions()
    deliveryOptions.addHeader("action", method.name)
    deliveryOptions.addHeader("paramTypes",
      method.parameterTypes
        .map { it.toGenericString() }
        .joinToString(",")
    )

    // TODO Add support for request filters

    val response = Sync.awaitResult<Message<Buffer>> {
      vertx.eventBus().send(serviceAddress, outBuffer.buffer, it)
    }

    if (response.body() != null && response.body().length() > 0) {
      val inBuffer = BufferInputStream(response.body())
      val parser = objectMapper.factory.createParser(inBuffer)
      val result = parser.readValueAs(method.returnType)

      parser.close()
      return result
    }

    if (method.returnType.isAssignableFrom(Void::class.java) ||
      method.returnType.isAssignableFrom(Unit::class.java))
      return Unit
    else
      return null
  }
}