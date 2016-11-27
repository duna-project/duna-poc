package io.duna.core.proxy

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
 * Classloader responsible for creating and loading remote service proxy classes.
 *
 * @author Carlos Eduardo Melo <[ceduardo.melo@redime.com.br]>
 */
class ProxyClassLoader(parent: ClassLoader) : ClassLoader(parent) {

  @Suppress("UNCHECKED_CAST")
  fun <T> loadProxyForService(serviceClass: Class<T>): Class<T> {

    if (!serviceClass.isInterface || !serviceClass.isAnnotationPresent(Contract::class.java)) {
       throw IllegalArgumentException("The 'serviceClass' argument must be a service contract interface.")
    }

    val injectAnnotation = AnnotationDescription.Builder.ofType(Inject::class.java).build()

    // @formatter:off
    return ByteBuddy()
        .subclass(Any::class.java)
        .implement(serviceClass)

        // Proxy fields
        .defineField("vertx", Vertx::class.java, Visibility.PRIVATE)
          .annotateField(injectAnnotation)
        .defineField("objectMapper", ObjectMapper::class.java, Visibility.PRIVATE)
          .annotateField(injectAnnotation)

        // Service contract method delegation
        .method(isDeclaredBy(serviceClass)).intercept(
          MethodDelegation
              .to(ServiceProxyInterceptor())
              .filter(not(isDeclaredBy(Any::class.java)))
        )

        .make()
        .load(this)
        .loaded as Class<T>
    // @formatter:on
  }
}