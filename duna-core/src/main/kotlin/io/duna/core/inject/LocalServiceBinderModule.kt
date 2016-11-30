package io.duna.core.inject

import com.google.inject.AbstractModule
import com.google.inject.BindingAnnotation
import com.google.inject.Scopes
import com.google.inject.UnsafeTypeLiteral
import com.google.inject.multibindings.Multibinder
import io.duna.core.classpath.ClasspathScanResults
import io.duna.core.service.AllServices
import io.duna.core.util.Services
import org.apache.logging.log4j.LogManager
import java.lang.reflect.Modifier
import javax.inject.Qualifier

internal class LocalServiceBinderModule : AbstractModule() {

  private val logger = LogManager.getLogger(LocalServiceBinderModule::class.java)

  override fun configure() {
    logger.info("Registering local services...")

    val localServicesSetBinder = Multibinder.newSetBinder(binder(), Any::class.java, AllServices::class.java)

    val localContracts = ClasspathScanResults.getLocalContracts()
        .map { Class.forName(it) }

    localContracts.forEach contractForEach@ { contractClass ->
      if (!contractClass.isInterface && !Modifier.isAbstract(contractClass.modifiers)) {
        logger.error("${contractClass.canonicalName} not registered. It isn't an interface nor an abstract class.")
        return@contractForEach
      }

      logger.info("\tContract ${contractClass.canonicalName}:")

      val contractTypeLiteral = UnsafeTypeLiteral(contractClass)

      ClasspathScanResults
          .getImplementationsInClasspath(contractClass)
          .map { Class.forName(it) }
          .forEach serviceForEach@ { serviceClass ->
            if (serviceClass.isInterface || Modifier.isAbstract(serviceClass.modifiers)) {
              logger.error("${serviceClass.canonicalName} isn't a concrete class, and therefore cannot be instantiated.")
              return@serviceForEach
            }

            logger.info("\t  • Service ${serviceClass.canonicalName}")

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

            localServicesSetBinder.addBinding()
                .to(serviceClass)
                .`in`(Scopes.SINGLETON)
          }
    }
  }
}