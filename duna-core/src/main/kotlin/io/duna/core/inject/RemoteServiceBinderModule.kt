package io.duna.core.inject

import com.google.inject.AbstractModule
import com.google.inject.UnsafeTypeLiteral
import io.duna.core.classpath.ClasspathScanResults
import io.duna.core.proxy.ProxyCallInterceptor
import io.duna.core.proxy.ProxyClassLoader
import org.apache.logging.log4j.LogManager
import java.lang.reflect.Modifier
import java.lang.reflect.Proxy

internal class RemoteServiceBinderModule : AbstractModule() {

  private val logger = LogManager.getLogger(LocalServiceBinderModule::class.java)

  /**
   * TODO Support qualifiers for proxies
   */
  override fun configure() {
    logger.info("Registering remote services...")

    val remoteContracts = ClasspathScanResults.getRemoteContracts()
        .map { Class.forName(it) }

    remoteContracts.forEach contractForEach@ { contract ->
      if (!contract.isInterface && !Modifier.isAbstract(contract.modifiers)) {
        logger.error("${contract.canonicalName} not registered. It isn't an interface nor an abstract class.")
        return@contractForEach
      }

      logger.info("\tContract ${contract.canonicalName}:")

      val contractTypeLiteral = UnsafeTypeLiteral(contract)
      val serviceProxy = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
          arrayOf(contract), ProxyCallInterceptor())

      logger.info("\t  • Proxy ${contract.canonicalName}")

      requestInjection(Proxy.getInvocationHandler(serviceProxy))
      bind(contractTypeLiteral).toInstance(serviceProxy)
    }
  }
}