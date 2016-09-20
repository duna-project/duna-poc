package io.duna.core.services

import io.duna.net.bytebuddy.implementation.bind.annotation.AllArguments
import io.duna.net.bytebuddy.implementation.bind.annotation.Origin
import java.lang.reflect.Method

object ServiceCallInterceptor {

  fun intercept(@AllArguments args: Array<Any>, @Origin method: Method) {
    println(method)
  }
}