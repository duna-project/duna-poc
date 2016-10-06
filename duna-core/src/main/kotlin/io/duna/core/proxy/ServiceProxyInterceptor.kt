package io.duna.core.proxy

import co.paralleluniverse.fibers.Suspendable
import com.google.inject.Inject
import io.vertx.core.Vertx
import net.bytebuddy.implementation.bind.annotation.AllArguments
import net.bytebuddy.implementation.bind.annotation.Origin

class ServiceProxyInterceptor(val address: String) {

  @Inject
  lateinit var vertx: Vertx

  @Suspendable
  fun intercept(@AllArguments args: Array<Any>, @Origin proxyClass: Class<*>): Any {
    return javaClass
  }
}