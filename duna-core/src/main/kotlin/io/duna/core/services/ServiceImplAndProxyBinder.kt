package io.duna.core.services

import com.google.inject.AbstractModule
import io.duna.net.bytebuddy.ByteBuddy
import io.duna.net.bytebuddy.implementation.MethodDelegation
import io.duna.net.bytebuddy.matcher.ElementMatchers
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult

class ServiceImplAndProxyBinder(val scanResult: ScanResult) : AbstractModule() {
  override fun configure() {
    // Create proxies for services without implementation in the classpath
    scanResult.getNamesOfClassesWithAnnotation(Service::class.java)
        .forEach {
          val clazz = Class.forName(it)
          if (clazz?.isInterface ?: false) {
            // Find implementations
            val impls = scanResult.getNamesOfClassesImplementing(it)
            when (impls.size) {
              0 -> createAndBindProxy(clazz)
              1 -> bindImplementation(impls[0], clazz)
              else -> throw Exception("Too many implementations")
            }
          }
        }
  }

  private fun bindImplementation(impl: String, iface: Class<*>) {
    val implClass = Class.forName(impl)
    bind(iface).to(implClass)
  }

  private fun createAndBindProxy(iface: Class<*>): Class<*> {
    val proxyClass = ByteBuddy()
        .subclass(iface)
        .method(ElementMatchers.isDeclaredBy(iface)).intercept(MethodDelegation.to(ServiceCallInterceptor))
        .make()
        .load(javaClass.getClassLoader())
        .loaded

    return proxyClass
  }
}