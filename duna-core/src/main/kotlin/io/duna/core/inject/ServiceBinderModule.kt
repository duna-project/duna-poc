package io.duna.core.inject

import com.google.inject.AbstractModule
import com.google.inject.BindingAnnotation
import com.google.inject.ManualTypeLiteral
import com.google.inject.Scopes
import io.duna.core.proxy.ProxyClassLoader
import io.duna.core.proxy.ServiceProxyFactory
import io.duna.core.service.Contract
import io.duna.core.service.Service
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult
import org.apache.logging.log4j.LogManager
import java.lang.reflect.Modifier
import javax.inject.Qualifier

class ServiceBinderModule(val scanResult: ScanResult) : AbstractModule() {

  private val logger = LogManager.getLogger(ServiceBinderModule::class.java)

  private val proxyClassLoader = ProxyClassLoader(javaClass.classLoader, ServiceProxyFactory())

  override fun configure() {
    logger.info("Registering services.")

    getServiceInterfaces().forEach {
      logger.info("Registering serviceClass ${it.canonicalName}")

      val implementations = scanResult.getNamesOfClassesImplementing(it.canonicalName).map { Class.forName(it) }

      when (implementations.size) {
        0 -> createAndBindProxy(it)
        else -> bindImplementations(it)
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  private fun bindImplementations(serviceClass: Class<*>) {
    getServiceImplementations(serviceClass).forEach { implementation ->
      val qualifier = implementation.declaredAnnotations.filter {
        it.annotationClass.java.isAnnotationPresent(Qualifier::class.java) ||
            it.javaClass.isAnnotationPresent(BindingAnnotation::class.java)
      }.singleOrNull()

      val typeLiteral = ManualTypeLiteral(serviceClass)

      if (qualifier == null) {
        bind(typeLiteral)
            .to(implementation)
            .asEagerSingleton()
      } else {
        bind(typeLiteral)
            .annotatedWith(qualifier)
            .to(implementation)
            .asEagerSingleton()
      }
    }
  }

  private fun createAndBindProxy(serviceClass: Class<*>) {
    val proxyClass = proxyClassLoader.proxyForService(serviceClass)
    val typeLiteral = ManualTypeLiteral(proxyClass)

    // Should bind to instance
    bind(typeLiteral)
        .to(proxyClass)
        .`in`(Scopes.SINGLETON)
  }

  private fun getServiceInterfaces(): List<Class<*>> {
    return scanResult.getNamesOfClassesWithAnnotation(Contract::class.java).map { Class.forName(it) }
  }

  private fun getServiceImplementations(service: Class<*>): List<Class<*>> {
    return scanResult.getNamesOfClassesImplementing(service)
        .map { Class.forName(it) }
        .filter { !it.isInterface && !Modifier.isAbstract(it.modifiers) }
        .filter { it.isAnnotationPresent(Service::class.java) }
  }
}