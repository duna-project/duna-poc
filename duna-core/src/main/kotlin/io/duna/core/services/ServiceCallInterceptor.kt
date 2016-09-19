package io.duna.core.services

import io.vertx.core.Vertx
import net.bytebuddy.implementation.bind.annotation.AllArguments
import net.bytebuddy.implementation.bind.annotation.Origin
import net.bytebuddy.jar.asm.commons.Method

object ServiceCallInterceptor {

  fun intercept(@AllArguments args: Array<Any>, @Origin method: Method) {
    println(method)
  }
}