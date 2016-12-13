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
        .subclass<Any>(method.declaringClass)
        .implement(MethodCallDelegator::class.java)
          .intercept(
            MethodCall
              .invoke(method)
              .withArgumentArrayElements(0)
              .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC)
          )
          .annotateMethod(suspendableAnnotation)
        .method(isDeclaredBy<ByteCodeElement>(method.declaringClass)
          .and(named<MethodDescription>(method.name)))
          .intercept(
            MethodCall
              .invoke(method)
              .on(delegate)
              .withAllArguments()
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
