package io.duna.core.service.impl

import io.duna.core.DunaException
import io.duna.core.service.Service
import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.implementation.Implementation
import net.bytebuddy.implementation.auxiliary.MethodCallProxy
import net.bytebuddy.matcher.ElementMatchers
import net.bytebuddy.matcher.ElementMatchers.*
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

object ServiceActionProxyFactory {

  val methodCache = ConcurrentHashMap<MethodDescription.InDefinedShape, Boolean>()

  fun create(target: Any, method: String, vararg paramTypes: Class<*>) {
    if (!target.javaClass.isAnnotationPresent(Service::class.java)) {
      throw DunaException("The action handler target must be a service.")
    }

    val targetType = TypeDescription.ForLoadedType(target.javaClass)

    val paramTypeDescriptions = paramTypes
        .map { TypeDescription.ForLoadedType(it) }

    val methodDescription = TypeDescription.ForLoadedType(target.javaClass)
        .declaredMethods
        .filter(cached(
            named<MethodDescription>(method)
                .and(takesArguments(paramTypeDescriptions)), methodCache))
        .only

    val callProxy = MethodCallProxy(Implementation.SpecialMethodInvocation.Simple.of(methodDescription, targetType),
        false)
  }
}