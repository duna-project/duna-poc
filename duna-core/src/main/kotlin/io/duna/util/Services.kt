/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.util

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

  @Suppress("RemoveCurlyBracesFromTemplate")
  fun getInternalServiceAddress(method: Method, separator: String = "."): String {
    if (fullAddressCache[method]?.isNotBlank() ?: false)
      return fullAddressCache[method]!!

    val serviceAddressPrefix = method.declaringClass.getAnnotation(Address::class.java)?.value
      ?: method.declaringClass.canonicalName

    val methodAddress = method.getAnnotation(Address::class.java)?.value
      ?: "(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)\\(.*\\)$".toRegex()
      .find(method.toString())!!
      .value

    fullAddressCache[method] = "${serviceAddressPrefix}${separator}${methodAddress}"

    return fullAddressCache[method]!!
  }

  @Suppress("RemoveCurlyBracesFromTemplate")
  fun getUniqueServiceAddress(method: Method, separator: String = "."): String {
    if (shortAddressCache[method]?.isNotBlank() ?: false)
      return shortAddressCache[method]!!

    val serviceAddressPrefix = method.declaringClass.getAnnotation(Address::class.java)?.value
      ?: method.declaringClass.canonicalName

    val methodAddress = method.getAnnotation(Address::class.java)?.value
      ?: run {
        var methodHashCode = 5
        methodHashCode = 37 * methodHashCode + method.hashCode()

        "${method.name}-${methodHashCode}"
      }

    shortAddressCache[method] = "${serviceAddressPrefix}${separator}${methodAddress}"

    return shortAddressCache[method]!!
  }
}
