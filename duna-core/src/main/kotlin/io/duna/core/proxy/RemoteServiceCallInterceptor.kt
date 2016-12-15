/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.proxy

import co.paralleluniverse.fibers.SuspendExecution
import com.fasterxml.jackson.databind.ObjectMapper
import io.duna.core.DunaException
import io.duna.io.BufferInputStream
import io.duna.io.BufferOutputStream
import io.duna.util.Services
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
import java.nio.charset.Charset
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

/**
 * Interceptor used to forward service calls to remote instances.
 *
 * @author Carlos Eduardo Melo <[ceduardo.melo@gmail.com]>
 */
internal object RemoteServiceCallInterceptor {

  val addressCache = ConcurrentHashMap<Method, String>()

  @RuntimeType
  @Throws(SuspendExecution::class, InterruptedException::class)
  fun intercept(@FieldValue("logger") logger: Logger,
                @FieldValue("objectMapper") objectMapper: ObjectMapper,
                @Origin(cache = true) method: Method,
                @AllArguments vararg arguments: Any?): Any? {

    logger.fine { "Intercepting method $method" }

    val vertx = Vertx.currentContext()?.owner() ?:
      throw DunaException("There isn't a vert.x context available.")

    val outBuffer = BufferOutputStream(Buffer.buffer(1024))
    val generator = objectMapper.factory.createGenerator(outBuffer)

    generator.writeStartObject()
    arguments.forEachIndexed { i, any -> generator.writeObjectField("$i", any) }
    generator.writeEndObject()

    generator.flush()
    generator.close()

    val serviceAddress = addressCache[method] ?:
      Services.getInternalServiceAddress(method)

    addressCache[method] = serviceAddress

    val deliveryOptions = DeliveryOptions()
    deliveryOptions.addHeader("action", method.name)
    deliveryOptions.addHeader("paramTypes",
      method.parameterTypes
        .map { it.toGenericString() }
        .joinToString(",")
    )

    // TODO Add support for request filters

    logger.info { "Sending request to $serviceAddress" }
    logger.finer { "Request: ${outBuffer.buffer.toString(Charset.defaultCharset())}"}

    val response = Sync.awaitResult<Message<Buffer>> {
      vertx.eventBus().send(serviceAddress, outBuffer.buffer, it)
    }

    logger.fine { "Response received from $serviceAddress" }
    logger.finer { "Response contents: ${response.body().toString(Charset.defaultCharset())}" }

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
