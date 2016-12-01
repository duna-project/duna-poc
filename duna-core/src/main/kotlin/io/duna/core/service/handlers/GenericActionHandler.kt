package io.duna.core.service.handlers

import co.paralleluniverse.fibers.Suspendable
import com.fasterxml.jackson.databind.ObjectMapper
import io.duna.core.io.BufferInputStream
import io.duna.core.io.BufferOutputStream
import io.duna.core.service.invocation.MethodCallDemuxing
import io.duna.core.service.invocation.ServiceCallDelegation
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.Message
import net.bytebuddy.ByteBuddy
import net.bytebuddy.implementation.FieldAccessor
import net.bytebuddy.matcher.ElementMatchers
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

  val serviceCallDelegation: ServiceCallDelegation

  init {
    serviceCallDelegation = ByteBuddy()
      .subclass(Any::class.java)
      .defineField("method", Method::class.java)
      .implement(ServiceCallDelegation::class.java)
      .intercept(FieldAccessor.ofField("method"))
      .method(ElementMatchers.named("invoke"))
      .intercept(MethodCallDemuxing(method))
      .make()
      .load(ClassLoader.getSystemClassLoader())
      .loaded
      .newInstance() as ServiceCallDelegation

    serviceCallDelegation.setMethod(method)
  }

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

    val result = serviceCallDelegation.invoke<Any>(service, *parameters)

    println("Result $result")
    if (method.returnType != Unit::class.java) {
      val outBuffer = BufferOutputStream(Buffer.buffer(1024))
      val generator = objectMapper.factory.createGenerator(outBuffer)

      generator.writeObject(result)

      println("Replying with " + outBuffer.buffer)
      event.reply(outBuffer.buffer)
    } else {
      println("Replying with null")
      event.reply(null)
    }
  }
}