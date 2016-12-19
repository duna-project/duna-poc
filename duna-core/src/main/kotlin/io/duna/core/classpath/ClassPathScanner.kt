/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.classpath

import com.typesafe.config.ConfigFactory
import io.duna.core.service.Contract
import io.duna.core.service.Service
import io.duna.port.Port
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult

/**
 * Scans the classpath for application plugins.
 */
object ClassPathScanner {

  private val config = ConfigFactory.load()

  var scanResult: ScanResult? =
    FastClasspathScanner(
      *config.getStringList("duna.classpath.ignore-packages")
        .map { "-$it" }
        .toTypedArray()
    ).scan()

  fun getPortExtensions(): List<String> = scanResult
    ?.getNamesOfClassesWithAnnotation(Port::class.java)!!

  fun getAllServices(): List<String> = scanResult
    ?.getNamesOfClassesWithAnnotation(Contract::class.java)!!

  fun getLocalServices(): List<String> = getAllServices()
      .filter { getImplementationsInClasspath(it).isNotEmpty() }

  fun getRemoteServices(): List<String> = getAllServices()
      .filter { getImplementationsInClasspath(it).isEmpty() }

  fun getImplementationsInClasspath(contract: String): Set<String> = scanResult
      ?.getNamesOfClassesImplementing(contract)!!
      .intersect(scanResult?.getNamesOfClassesWithAnnotation(Service::class.java)!!)

  fun getImplementationsInClasspath(contract: Class<*>): Set<String> = scanResult
      ?.getNamesOfClassesImplementing(contract)!!
      .intersect(scanResult?.getNamesOfClassesWithAnnotation(Service::class.java)!!)
}
