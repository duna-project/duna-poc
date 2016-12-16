/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.implementation

import co.paralleluniverse.fibers.Suspendable
import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.ByteCodeElement
import net.bytebuddy.description.annotation.AnnotationDescription
import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.implementation.MethodCall
import net.bytebuddy.implementation.bytecode.assign.Assigner
import net.bytebuddy.matcher.ElementMatchers.isDeclaredBy
import net.bytebuddy.matcher.ElementMatchers.named
import java.lang.reflect.Method

interface MethodCallDelegator<T> {

  fun invoke(vararg args: Any?): Any?

  companion object {

    @Suppress("UNCHECKED_CAST")
    fun <T> to(delegate: T, method: Method): MethodCallDelegator<T> {
      val suspendableAnnotation = AnnotationDescription.Builder.ofType(Suspendable::class.java).build()

      // @formatter:off
      return ByteBuddy()
        .subclass<Any>(Any::class.java)
        .implement(MethodCallDelegator::class.java)
          .intercept(
            MethodCall
              .invoke(method)
              .on(delegate)
              .withArgumentArrayElements(0, method.parameterCount)
              .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC)
          )
          .annotateMethod(suspendableAnnotation)
        .make()
        .load(MethodCallDelegator::class.java.classLoader)
        .loaded
        .newInstance() as MethodCallDelegator<T>
      // @formatter:on
    }
  }
}
