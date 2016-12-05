package io.duna.core.service.handlers

import co.paralleluniverse.fibers.Suspendable
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import io.duna.core.io.BufferInputStream
import io.duna.core.io.BufferOutputStream
import io.duna.core.implementation.invocation.MethodCallDemuxing
import io.duna.core.implementation.invocation.ServiceCallDelegation
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.Message
import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.annotation.AnnotationDescription
import net.bytebuddy.implementation.FieldAccessor
import net.bytebuddy.matcher.ElementMatchers
import java.lang.reflect.Method
import java.util.*
import java.util.logging.Logger

/**
 * Handles a request goTo a service and replies the result.
 */
internal class GenericActionHandler<T>(private val service: T,
                                       private val method: Method) : Handler<Message<Buffer>> {

  @Inject
  lateinit var objectMapper: ObjectMapper

  @Inject
  lateinit var logger: Logger

  val serviceCallDelegation: ServiceCallDelegation

  init {
    val suspendableAnnotation = AnnotationDescription.Builder.ofType(Suspendable::class.java).build()

    // @formatter:off
    serviceCallDelegation = ByteBuddy()
      .subclass(Any::class.java)
      .defineField("method", Method::class.java)
      .implement(ServiceCallDelegation::class.java)
        .intercept(FieldAccessor.ofField("method"))
      .method(ElementMatchers.named("invoke"))
        .intercept(MethodCallDemuxing(method))
        .annotateMethod(suspendableAnnotation)
      .make()
      .load(ClassLoader.getSystemClassLoader())
      .loaded
      .newInstance() as ServiceCallDelegation
    // @formatter:on

    serviceCallDelegation.setMethod(method)
  }

  @Suspendable
  override fun handle(event: Message<Buffer>) {
    logger.fine { "Handling request to " +
      "${method.declaringClass.name}.${method.name}(${method.parameterTypes.joinToString(", ")})" }

    val inBuffer = BufferInputStream(event.body())
    val parser = objectMapper.factory.createParser(inBuffer)

    val parameters = arrayOfNulls<Any>(method.parameterCount)

    parser.nextToken() // START_OBJECT
    method.parameterTypes.forEachIndexed { i, clazz ->
      parser.nextToken() // FIELD_NAME
      parser.nextToken() // FIELD_VALUE
      parameters[i] = parser.readValueAs(clazz)
    }

    logger.finer { "Parameters received: [" + parameters.joinToString(",") + "]" }

    val result = serviceCallDelegation.invoke<Any>(service, *parameters)

    if (method.returnType != Unit::class.java) {
      val outBuffer = BufferOutputStream(Buffer.buffer(1024))
      val generator = objectMapper.factory.createGenerator(outBuffer)

      generator.writeObject(result)

      logger.finer { "Reply to ${event.replyAddress()}: $result" }
      event.reply(outBuffer.buffer)
    } else {
      logger.finer { "Reply to ${event.replyAddress()}: null" }
      event.reply(null)
    }
  }
}