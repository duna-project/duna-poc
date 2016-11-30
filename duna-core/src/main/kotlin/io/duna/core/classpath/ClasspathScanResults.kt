package io.duna.core.classpath

import io.duna.core.service.Contract
import io.duna.core.service.Service
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult

internal object ClasspathScanResults {

  private var scanResult: ScanResult = FastClasspathScanner()
      .scan(Runtime.getRuntime().availableProcessors())

  fun getAllContracts(): List<String> = scanResult
      .getNamesOfClassesWithAnnotation(Contract::class.java)

  fun getLocalContracts(): List<String> = getAllContracts()
      .filter { getImplementationsInClasspath(it).isNotEmpty() }

  fun getRemoteContracts(): List<String> = getAllContracts()
      .filter { getImplementationsInClasspath(it).isEmpty() }

  fun getImplementationsInClasspath(contract: String): Set<String> = scanResult
      .getNamesOfClassesImplementing(contract)
      .intersect(scanResult.getNamesOfClassesWithAnnotation(Service::class.java))

  fun getImplementationsInClasspath(contract: Class<*>): Set<String> = scanResult
      .getNamesOfClassesImplementing(contract)
      .intersect(scanResult.getNamesOfClassesWithAnnotation(Service::class.java))
}