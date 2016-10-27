package io.duna.core.proxy

import com.fasterxml.jackson.databind.ObjectMapper
import io.duna.core.proxy.ServiceProxyInterceptor
import io.duna.core.proxy_gen.ServiceProxyFactory
import io.duna.core.service.Contract
import io.vertx.core.Vertx
import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.annotation.AnnotationDescription
import net.bytebuddy.description.modifier.ModifierContributor
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.implementation.FieldAccessor
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.matcher.ElementMatchers
import net.bytebuddy.matcher.ElementMatchers.*
import java.lang.reflect.Modifier
import javax.inject.Inject

/**
 * Classloader responsible for creating and loading remote service proxy_gen classes.
 *
 * @author Carlos Eduardo Melo <[ceduardo.melo@redime.com.br]>
 */
class ProxyClassLoader(parent: ClassLoader,
                       private val proxyFactory: ServiceProxyFactory) : ClassLoader(parent) {

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

//  @Suppress("UNCHECKED_CAST")
//  fun <T> loadProxyForService(serviceClass: Class<T>): Class<T> {
//    if (!serviceClass.isAnnotationPresent(Contract::class.java)) {
//      throw RuntimeException("[ERR-0001] ${serviceClass.canonicalName} isn't a valid service contract.")
//    }
//
//    if (!(serviceClass.isInterface || Modifier.isAbstract(serviceClass.modifiers))) {
//      throw RuntimeException("[ERR-0002] Error registering ${serviceClass.canonicalName}: cannot register concrete classes " +
//          "as remote services.")
//    }
//
//    val bytes = proxyFactory.generateFor(serviceClass)
//
//    val clazz = defineClass(proxyFactory.namingStrategy.getProxyClassName(serviceClass),
//        bytes, 0, bytes.size)
//    resolveClass(clazz)
//
//    return clazz as Class<T>
//  }
}