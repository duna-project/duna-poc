package io.duna.core.inject

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import com.google.inject.UnsafeTypeLiteral
import io.duna.core.classpath.ClasspathScanner
import io.duna.core.util.Services
import java.lang.reflect.Modifier
import java.util.logging.LogManager

internal object LocalServiceBinderModule : AbstractModule() {

  @JvmStatic
  private val logger = LogManager.getLogManager()
    .getLogger(LocalServiceBinderModule::class.java.name)

  override fun configure() {
    logger.info { "Binding local services" }

    val localContracts = ClasspathScanner.getLocalServices()
        .map { Class.forName(it) }

    localContracts.forEach contractForEach@ { contractClass ->
      if (!contractClass.isInterface && !Modifier.isAbstract(contractClass.modifiers)) {
        logger.warning { "Unable to bind ${contractClass.canonicalName}. " +
          "Contracts must be either an interface or abstract class." }
        return@contractForEach
      }

      val contractTypeLiteral = UnsafeTypeLiteral(contractClass)

      ClasspathScanner
          .getImplementationsInClasspath(contractClass)
          .map { Class.forName(it) }
          .forEach serviceForEach@ { serviceClass ->
            if (serviceClass.isInterface || Modifier.isAbstract(serviceClass.modifiers)) {
              logger.warning { "Unable to bind ${serviceClass.canonicalName}. " +
                "Implementations must be instantiable." }
              return@serviceForEach
            }

            val qualifier = Services.getQualifier(serviceClass)

            if (qualifier != null) {
              bind(contractTypeLiteral)
                  .annotatedWith(qualifier)
                  .to(serviceClass)
                  .`in`(Scopes.SINGLETON)
            } else {
              bind(contractTypeLiteral)
                  .to(serviceClass)
                  .`in`(Scopes.SINGLETON)
            }

            logger.info { "Bound ${contractClass.canonicalName} -> ${serviceClass.canonicalName}" }
          }
    }

    logger.info { "Local services bound" }
  }
}