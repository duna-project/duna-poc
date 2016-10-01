package io.duna.core.proxy

import com.google.inject.AbstractModule
import io.duna.core.service.Service
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult

class ServiceImplAndProxyBinder(val scanResult: ScanResult) : AbstractModule() {

  override fun configure() {
    // Create proxies for service without implementation in the classpath
    scanResult.getNamesOfClassesWithAnnotation(Service::class.java)
        .forEach {
          val clazz = Class.forName(it)
          if (clazz?.isInterface ?: false) {
            // Find implementations
            val impls = scanResult.getNamesOfClassesImplementing(it)
            when (impls.size) {
              0 -> bind(clazz).to(ServiceProxyFactory.create(clazz))
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
}