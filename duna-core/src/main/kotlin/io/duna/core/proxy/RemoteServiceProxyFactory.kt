/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.proxy

import co.paralleluniverse.fibers.Suspendable
import com.fasterxml.jackson.databind.ObjectMapper
import io.duna.core.service.Contract
import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.annotation.AnnotationDescription
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.matcher.ElementMatchers.isDeclaredBy
import net.bytebuddy.matcher.ElementMatchers.not
import java.util.logging.Logger
import javax.inject.Inject

/**
 * Factory responsible for loading proxy classes for remote contracts.
 *
 * @author Carlos Eduardo Melo <[ceduardo.melo@redime.com.br]>
 */
class RemoteServiceProxyFactory() {

  @Suppress("UNCHECKED_CAST")
  fun <T> loadProxyForService(contractClass: Class<T>): Class<T> {

    if (!contractClass.isInterface || !contractClass.isAnnotationPresent(Contract::class.java)) {
       throw IllegalArgumentException("The @Contract must be an interface or abstract class.")
    }

    // @Inject
    val injectAnnotation = AnnotationDescription.Builder
      .ofType(Inject::class.java).build()

    // @Suspendable
    val suspendableAnnotation = AnnotationDescription.Builder
      .ofType(Suspendable::class.java).build()

    // @GeneratedProxy
    val generatedProxyAnnotation = AnnotationDescription.Builder
      .ofType(GeneratedProxy::class.java).build()

    // @formatter:off
    return ByteBuddy()
      .subclass(Any::class.java)
      .implement(contractClass)
      .annotateType(generatedProxyAnnotation)

      // Proxy fields
      .defineField("logger", Logger::class.java, Visibility.PRIVATE)
        .annotateField(injectAnnotation)
      .defineField("objectMapper", ObjectMapper::class.java, Visibility.PRIVATE)
        .annotateField(injectAnnotation)

      // Service contract method delegation
      .method(isDeclaredBy(contractClass))
        .intercept(
          MethodDelegation
            .to(RemoteServiceCallInterceptor)
            .filter(not(isDeclaredBy(Any::class.java)))
        )
        .annotateMethod(suspendableAnnotation)

      .make()
      .load(this.javaClass.classLoader)
      .loaded as Class<T>
    // @formatter:on
  }
}
