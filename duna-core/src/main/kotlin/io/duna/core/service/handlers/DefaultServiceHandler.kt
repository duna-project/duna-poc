/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.service.handlers

import co.paralleluniverse.fibers.SuspendExecution
import co.paralleluniverse.fibers.Suspendable
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.assistedinject.Assisted
import io.duna.core.implementation.invocation.DestructuringMethodCall
import io.duna.core.service.ServiceCallDelegation
import io.duna.core.io.BufferInputStream
import io.duna.core.io.BufferOutputStream
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.Message
import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.annotation.AnnotationDescription
import net.bytebuddy.implementation.FieldAccessor
import net.bytebuddy.matcher.ElementMatchers
import java.lang.reflect.Method
import java.util.logging.Logger
import javax.inject.Inject

/**
 * Handles a request to a service and replies the result.
 */
internal class DefaultServiceHandler<T>
  @Inject constructor(@Assisted private val service: T,
                      @Assisted private val method: Method,
                      private val objectMapper: ObjectMapper,
                      private val logger: Logger) : Handler<Message<Buffer>> {

  val serviceCallDelegation: ServiceCallDelegation<T>

  init {
    val suspendableAnnotation = AnnotationDescription.Builder.ofType(Suspendable::class.java).build()

    // @formatter:off
    @Suppress("UNCHECKED_CAST")
    serviceCallDelegation = ByteBuddy()
      .subclass(Any::class.java)
      .defineField("method", Method::class.java)
      .implement(ServiceCallDelegation::class.java)
        .intercept(FieldAccessor.ofField("method"))
      .method(ElementMatchers.named("invoke"))
        .intercept(DestructuringMethodCall(method))
        .annotateMethod(suspendableAnnotation)
      .make()
      .load(ClassLoader.getSystemClassLoader())
      .loaded
      .newInstance() as ServiceCallDelegation<T>
    // @formatter:on

    serviceCallDelegation.setMethod(method)
  }

  @Suspendable
  override fun handle(event: Message<Buffer>) {
    logger.fine {
      "Handling request to " +
        "${method.declaringClass.name}.${method.name}(${method.parameterTypes.joinToString(", ")})"
    }

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

    val result = serviceCallDelegation.invoke(service, *parameters)

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

  interface Factory {
    fun create(service: Any, method: Method): DefaultServiceHandler<Any>
  }
}
