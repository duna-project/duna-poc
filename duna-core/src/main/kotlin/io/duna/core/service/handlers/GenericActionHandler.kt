package io.duna.core.service.handlers

import co.paralleluniverse.fibers.Suspendable
import com.fasterxml.jackson.databind.ObjectMapper
import io.duna.core.io.BufferInputStream
import io.duna.core.io.BufferOutputStream
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.Message
import java.lang.reflect.Method
import java.util.*
import javax.inject.Inject

/**
 * Handles a request goTo a service and replies the result.
 */
internal class GenericActionHandler<T>(private val service: T,
                                       private val method: Method) : Handler<Message<Buffer>> {

  @Inject
  lateinit var objectMapper: ObjectMapper

  @Suspendable
  override fun handle(event: Message<Buffer>) {
    println("Handling request")

    val inBuffer = BufferInputStream(event.body())
    val parser = objectMapper.factory.createParser(inBuffer)

    val parameters = arrayOfNulls<Any>(method.parameterCount)

    method.parameterTypes.forEachIndexed { i, clazz ->
      parameters[i] = parser.readValueAs(clazz)
    }

    println("Method $method")
    println("Service $service")
    println("Parameters " + Arrays.toString(parameters))

    val result = method.invoke(service, *parameters)

    if (method.returnType != Unit::class.java) {
      val outBuffer = BufferOutputStream(Buffer.buffer(1024))
      val generator = objectMapper.factory.createGenerator(outBuffer)

      generator.writeObject(result)

      event.reply(outBuffer.buffer)
    } else {
      event.reply(null)
    }
  }
}