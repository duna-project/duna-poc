/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.util

import com.google.common.primitives.Primitives
import com.google.inject.BindingAnnotation
import io.duna.core.service.Address
import io.duna.core.service.Service
import java.lang.reflect.Method
import javax.inject.Qualifier

object Services {

  val fullAddressCache = mutableMapOf<Method, String>()

  val shortAddressCache = mutableMapOf<Method, String>()

  fun getQualifier(serviceClass: Class<*>): Annotation? =
      serviceClass.annotations
          .filter { it.javaClass.isAssignableFrom(Service::class.java) }
          .find {
            it.javaClass.isAnnotationPresent(Qualifier::class.java) ||
                it.javaClass.isAnnotationPresent(BindingAnnotation::class.java)
          }

  fun getInternalServiceAddress(method: Method, separator: String = "."): String {
    if (fullAddressCache[method]?.isNotBlank() ?: false)
      return fullAddressCache[method]!!

    val methodAddress = StringBuilder()
    methodAddress
      .append("${method.declaringClass.name}.${method.name}(")
      .append(
        method.parameterTypes
          .map { Primitives.wrap(it) }
          .map { it.name.replace("java\\.lang\\.", "") }
          .joinToString(",")
      )
      .append(")")

    fullAddressCache[method] = methodAddress.toString()

    return fullAddressCache[method]!!
  }
}
