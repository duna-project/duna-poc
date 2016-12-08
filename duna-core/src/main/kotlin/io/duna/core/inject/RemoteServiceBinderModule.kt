/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.inject

import com.google.inject.AbstractModule
import com.google.inject.UnsafeTypeLiteral
import io.duna.core.classpath.ClasspathScanner
import io.duna.core.proxy.RemoteServiceProxyFactory
import java.lang.reflect.Modifier
import java.util.logging.LogManager

internal object RemoteServiceBinderModule : AbstractModule() {

  @JvmStatic
  private val logger = LogManager.getLogManager()
    .getLogger(RemoteServiceBinderModule::class.java.name)

  private val classLoader = RemoteServiceProxyFactory()

  /**
   * TODO Support qualifiers for proxies
   */
  override fun configure() {
    logger.info { "Binding proxies for remote services" }

    val remoteContracts = ClasspathScanner.getRemoteServices()
        .map { Class.forName(it) }

    remoteContracts.forEach contractForEach@ { contractClass ->
      if (!contractClass.isInterface && !Modifier.isAbstract(contractClass.modifiers)) {
        logger.warning { "Unable to bind ${contractClass.canonicalName}. " +
          "Contracts must be either an interface or abstract class." }
        return@contractForEach
      }

      val contractTypeLiteral = UnsafeTypeLiteral(contractClass)
      val proxyClass = classLoader.loadProxyForService(contractClass).newInstance()

      logger.info { "Bound proxy for ${contractClass.canonicalName}" }

      requestInjection(proxyClass)
      bind(contractTypeLiteral).toInstance(proxyClass)
    }

    logger.info { "Proxies for remote services bound" }
  }
}
