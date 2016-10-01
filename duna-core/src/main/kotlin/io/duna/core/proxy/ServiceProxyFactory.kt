package io.duna.core.proxy

import io.duna.core.service.ServiceProxy
import net.bytebuddy.ByteBuddy
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.matcher.ElementMatchers

object ServiceProxyFactory {

  fun create(serviceClazz: Class<*>): Class<*> {
    val proxyClass = ByteBuddy()
        .subclass(ServiceProxy::class.java)
        .implement(serviceClazz)
        .method(ElementMatchers.isDeclaredBy(serviceClazz))
          .intercept(MethodDelegation.to(ServiceProxyFactory))
        .make()
        .load(javaClass.classLoader)
        .loaded

    return proxyClass
  }
}