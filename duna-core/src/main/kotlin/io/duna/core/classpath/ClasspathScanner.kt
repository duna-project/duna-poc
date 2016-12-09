/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.classpath

import io.duna.core.service.Contract
import io.duna.core.service.Service
import io.duna.core.vertx.BridgeVerticle
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult
import io.vertx.core.Verticle

/**
 * Scans the classpath for contracts and services.
 */
object ClasspathScanner {

  private var scanResult: ScanResult = FastClasspathScanner()
      .scan(Runtime.getRuntime().availableProcessors())

  fun getBridgeVerticles(): Set<String> = scanResult
    .getNamesOfClassesWithAnnotation(BridgeVerticle::class.java)
    .intersect(scanResult.getNamesOfClassesImplementing(Verticle::class.java))

  fun getAllServices(): List<String> = scanResult
      .getNamesOfClassesWithAnnotation(Contract::class.java)

  fun getLocalServices(): List<String> = getAllServices()
      .filter { getImplementationsInClasspath(it).isNotEmpty() }

  fun getRemoteServices(): List<String> = getAllServices()
      .filter { getImplementationsInClasspath(it).isEmpty() }

  fun getImplementationsInClasspath(contract: String): Set<String> = scanResult
      .getNamesOfClassesImplementing(contract)
      .intersect(scanResult.getNamesOfClassesWithAnnotation(Service::class.java))

  fun getImplementationsInClasspath(contract: Class<*>): Set<String> = scanResult
      .getNamesOfClassesImplementing(contract)
      .intersect(scanResult.getNamesOfClassesWithAnnotation(Service::class.java))
}
