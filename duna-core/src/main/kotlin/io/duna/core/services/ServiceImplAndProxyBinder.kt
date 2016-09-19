package io.duna.core.services

import com.google.inject.AbstractModule
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult
import net.bytebuddy.ByteBuddy

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

  private fun createAndBindProxy(iface: Class<*>) {
    val proxyClass = ByteBuddy()
  }
}