package io.duna.core.proxy

import co.paralleluniverse.fibers.Suspendable
import com.fasterxml.jackson.databind.ObjectMapper
import io.duna.core.service.Contract
import io.vertx.core.Vertx
import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.annotation.AnnotationDescription
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.matcher.ElementMatchers.isDeclaredBy
import net.bytebuddy.matcher.ElementMatchers.not
import javax.inject.Inject

/**
 * Factory responsible for loading proxy classes for remote contracts.
 *
 * @author Carlos Eduardo Melo <[ceduardo.melo@redime.com.br]>
 */
class RemoteServiceProxyFactory(private val proxyNamingStrategy: ProxyNamingStrategy) {

  constructor(): this(DefaultProxyNamingStrategy())

  @Suppress("UNCHECKED_CAST")
  fun <T> loadProxyForService(contractClass: Class<T>): Class<T> {

    if (!contractClass.isInterface || !contractClass.isAnnotationPresent(Contract::class.java)) {
       throw IllegalArgumentException("The @Contract must be an interface or abstract class.")
    }

    val injectAnnotation = AnnotationDescription.Builder.ofType(Inject::class.java).build()
    val suspendableAnnotation = AnnotationDescription.Builder.ofType(Suspendable::class.java).build()

    // @formatter:off
    return ByteBuddy()
      .subclass(Any::class.java)
      .implement(contractClass)
      .name(proxyNamingStrategy.getProxyName(contractClass))

      // Proxy fields
      .defineField("vertx", Vertx::class.java, Visibility.PRIVATE)
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