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
import java.lang.reflect.Proxy
import javax.inject.Inject

/**
 * Classloader responsible for creating and loading remote service proxy classes.
 *
 * @author Carlos Eduardo Melo <[ceduardo.melo@redime.com.br]>
 */
class ProxyClassLoader(parent: ClassLoader,
                       private val proxyNamingStrategy: ProxyNamingStrategy) : ClassLoader(parent) {

  constructor(parent: ClassLoader): this(parent, DefaultProxyNamingStrategy())

  @Suppress("UNCHECKED_CAST")
  fun <T> loadProxyForService(contract: Class<T>): Class<T> {

    if (!contract.isInterface || !contract.isAnnotationPresent(Contract::class.java)) {
       throw IllegalArgumentException("The 'contract' argument must be a service contract interface.")
    }

//    val injectAnnotation = AnnotationDescription.Builder.ofType(Inject::class.java).build()

    // @formatter:off
    return this.javaClass as Class<T>

//    ByteBuddy()
//        .subclass(GenericServiceProxy::class.java)
//        .implement(contract)
//        .name(proxyNamingStrategy.getProxyName(contract))
//
//        // Proxy fields
//        .defineField("vertx", Vertx::class.java, Visibility.PRIVATE)
//          .annotateField(injectAnnotation)
//        .defineField("objectMapper", ObjectMapper::class.java, Visibility.PRIVATE)
//          .annotateField(injectAnnotation)
//
//        // Service contract method delegation
//        .method(isDeclaredBy(contract)).intercept(
//          MethodDelegation
//              .goTo(ProxyCallInterceptor())
//              .filter(not(isDeclaredBy(Any::class.java)))
//        )
//
//        .make()
//        .load(this)
//        .loaded as Class<T>
    // @formatter:on
  }
}