package io.duna.core.inject

import com.google.inject.AbstractModule
import com.google.inject.BindingAnnotation
import com.google.inject.Scopes
import com.google.inject.UnsafeTypeLiteral
import com.google.inject.multibindings.Multibinder
import io.duna.core.classpath.ClasspathScanner
import io.duna.core.service.AllServices
import io.duna.core.util.Services
import org.apache.logging.log4j.LogManager
import java.lang.reflect.Modifier
import javax.inject.Qualifier

internal class LocalServiceBinderModule : AbstractModule() {

  private val logger = LogManager.getLogger(LocalServiceBinderModule::class.java)

  override fun configure() {
    logger.info("Binding local services")

    val localContracts = ClasspathScanner.getLocalServices()
        .map { Class.forName(it) }

    localContracts.forEach contractForEach@ { contractClass ->
      if (!contractClass.isInterface && !Modifier.isAbstract(contractClass.modifiers)) {
        logger.error("Unable to bind ${contractClass.canonicalName}. " +
          "Contracts must be either an interface or abstract class.")
        return@contractForEach
      }

      val contractTypeLiteral = UnsafeTypeLiteral(contractClass)

      ClasspathScanner
          .getImplementationsInClasspath(contractClass)
          .map { Class.forName(it) }
          .forEach serviceForEach@ { serviceClass ->
            if (serviceClass.isInterface || Modifier.isAbstract(serviceClass.modifiers)) {
              logger.error("Unable to bind ${serviceClass.canonicalName}. " +
                "Implementations must be instantiable.")
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

            logger.info("Bound ${contractClass.canonicalName} -> ${serviceClass.canonicalName}")
          }
    }

    logger.info("Local services bound")
  }
}