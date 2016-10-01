package io.duna.core.proxy

import co.paralleluniverse.fibers.Suspendable
import com.google.inject.Inject
import io.duna.core.service.Address
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.ext.sync.Sync
import net.bytebuddy.implementation.bind.annotation.AllArguments
import net.bytebuddy.implementation.bind.annotation.Origin
import net.bytebuddy.implementation.bind.annotation.RuntimeType

object ServiceCallInterceptor {

  @Inject
  lateinit var vertx: Vertx

  @Suspendable
  fun intercept(@AllArguments args: Array<Any>, @RuntimeType @Origin clazz: Class<*>): Any {
    val serviceClazz = clazz.interfaces[0]

    val address = serviceClazz.getAnnotation(Address::class.java)?.address ?: serviceClazz.canonicalName
    val eventBus = vertx.eventBus()

    val reply: Message<Any> = Sync.awaitResult { h -> eventBus.send(address, args, h) }
    return reply.body()
  }
}