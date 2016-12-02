package io.duna.core.util

import com.google.inject.BindingAnnotation
import io.duna.core.service.Address
import io.duna.core.service.Service
import java.lang.reflect.Method
import javax.inject.Qualifier
import kotlin.reflect.jvm.kotlinFunction

object Services {

  fun getQualifier(serviceClass: Class<*>): Annotation? =
      serviceClass.annotations
          .filter { it.javaClass.isAssignableFrom(Service::class.java) }
          .find {
            it.javaClass.isAnnotationPresent(Qualifier::class.java) ||
                it.javaClass.isAnnotationPresent(BindingAnnotation::class.java)
          }

  fun getServiceAddress(method: Method): String {
    val serviceAddressPrefix = method.declaringClass.getAnnotation(Address::class.java)?.value
      ?: method.declaringClass.canonicalName

    val methodAddress = method.getAnnotation(Address::class.java)?.value
      ?: "(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)\\(.*\\)$".toRegex()
      .find(method.toString())!!
      .value

    return "$serviceAddressPrefix.$methodAddress"
  }
}