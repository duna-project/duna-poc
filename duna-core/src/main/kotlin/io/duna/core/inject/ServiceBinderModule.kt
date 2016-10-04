package io.duna.core.inject

import com.google.inject.AbstractModule
import com.google.inject.BindingAnnotation
import com.google.inject.ManualTypeLiteral
import io.duna.core.service.Contract
import io.duna.core.service.Service
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult
import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.annotation.AnnotationDescription
import org.apache.logging.log4j.LogManager
import java.lang.reflect.Modifier
import java.net.URLClassLoader
import javax.inject.Qualifier

class ServiceBinderModule(val scanResult: ScanResult) : AbstractModule() {

  private val logger = LogManager.getLogger(ServiceBinderModule::class.java)

  private val proxyClassLoader = URLClassLoader(null, javaClass.classLoader)

  override fun configure() {
    logger.info("Registering services.")

    getServiceInterfaces().forEach {
      logger.info("Registering service ${it.canonicalName}")

      val implementations = scanResult.getNamesOfClassesImplementing(it.canonicalName).map { Class.forName(it) }

      when (implementations.size) {
        0 -> createAndBindProxy(it)
        else -> bindImplementations(it)
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  private fun bindImplementations(service: Class<*>) {
    getServiceImplementations(service).forEach { implementation ->
      val qualifier = implementation.declaredAnnotations.filter {
        it.annotationClass.java.isAnnotationPresent(Qualifier::class.java) ||
            it.javaClass.isAnnotationPresent(BindingAnnotation::class.java)
      }.singleOrNull()

      val typeLiteral = ManualTypeLiteral(service)

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

  private fun createAndBindProxy(service: Class<*>) {
    val proxyClass = ByteBuddy()
      .subclass(Any::class.java)
      .implement(service)
      .annotateType(AnnotationDescription.Builder
          .ofType(Service::class.java)
          .build())
      .make()
      .load(proxyClassLoader)
      .loaded

    val typeLiteral = ManualTypeLiteral(service)

    bind(typeLiteral)
      .to(proxyClass)
      .asEagerSingleton()
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