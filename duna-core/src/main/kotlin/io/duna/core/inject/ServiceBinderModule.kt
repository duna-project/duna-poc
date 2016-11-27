package io.duna.core.inject

import com.google.inject.AbstractModule
import com.google.inject.BindingAnnotation
import com.google.inject.ManualTypeLiteral
import com.google.inject.Scopes
import com.google.inject.multibindings.Multibinder
import io.duna.core.proxy.ProxyClassLoader
import io.duna.core.service.Contract
import io.duna.core.service.Service
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult
import org.apache.logging.log4j.LogManager
import java.lang.reflect.Modifier
import javax.inject.Qualifier

/**
 * Binds service contracts to its implementations or to a proxy for remote instances.
 *
 * This module uses a classpath scanning result to bind local and remote services to
 * their respective contracts, in order to be injected at dependant locations.
 *
 * @param scanResult the classpath scanning result.
 *
 * @author Carlos Eduardo Melo <[ceduardo.melo@redime.com.br]>
 */
class ServiceBinderModule(val scanResult: ScanResult) : AbstractModule() {

  private val logger = LogManager.getLogger(ServiceBinderModule::class.java)

  private val proxyClassLoader = ProxyClassLoader(javaClass.classLoader)

  private lateinit var servicesMultibinder: Multibinder<Any>

  override fun configure() {
    servicesMultibinder = Multibinder.newSetBinder(binder(),
        Any::class.java,
        Service::class.java)

    logger.info("Registering services")

    getServiceContracts().forEach {
      logger.info("Binding service contract ${it.canonicalName}")

      val implementations = scanResult.getNamesOfClassesImplementing(it.canonicalName)
          .map { Class.forName(it) }

      when (implementations.size) {
        0 -> createAndBindProxy(it)
        else -> bindImplementations(it)
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  private fun bindImplementations(serviceContract: Class<*>) {
    getServiceImplementations(serviceContract).forEach { implementation ->
      val qualifier = implementation.declaredAnnotations.filter {
        it.annotationClass.java.isAnnotationPresent(Qualifier::class.java) ||
            it.javaClass.isAnnotationPresent(BindingAnnotation::class.java)
      }.singleOrNull()

      val contractTypeLiteral = ManualTypeLiteral(serviceContract)

      if (qualifier == null) {
        bind(contractTypeLiteral)
            .to(implementation)
            .`in`(Scopes.SINGLETON)
      } else {
        bind(contractTypeLiteral)
            .annotatedWith(qualifier)
            .to(implementation)
            .`in`(Scopes.SINGLETON)
      }

      servicesMultibinder
          .addBinding()
          .to(implementation)
          .`in`(Scopes.SINGLETON)
    }
  }

  private fun createAndBindProxy(serviceClass: Class<*>) {
    val proxyClass = proxyClassLoader.loadProxyForService(serviceClass)
    val typeLiteral = ManualTypeLiteral(proxyClass)

    // Should bind to instance
    bind(typeLiteral)
        .to(proxyClass)
        .`in`(Scopes.SINGLETON)
  }

  private fun getServiceContracts(): List<Class<*>> {
    return scanResult.getNamesOfClassesWithAnnotation(Contract::class.java).map { Class.forName(it) }
  }

  private fun getServiceImplementations(service: Class<*>): List<Class<*>> {
    return scanResult.getNamesOfClassesImplementing(service)
        .map { Class.forName(it) }
        .filter { !it.isInterface && !Modifier.isAbstract(it.modifiers) }
        .filter { it.isAnnotationPresent(Service::class.java) }
  }
}