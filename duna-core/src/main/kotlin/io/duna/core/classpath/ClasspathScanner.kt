package io.duna.core.classpath

import io.duna.core.service.Contract
import io.duna.core.service.Service
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult

/**
 * Scans the classpath for contracts and services.
 */
internal object ClasspathScanner {

  private var scanResult: ScanResult = FastClasspathScanner()
      .scan(Runtime.getRuntime().availableProcessors())

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