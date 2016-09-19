package io.duna.core.inject

import com.google.inject.AbstractModule
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult
import io.vertx.core.Verticle

class InjectorModuleBinder(val scanResult: ScanResult) : AbstractModule() {
  override fun configure() {
    scanResult
        .getNamesOfClassesWithAnnotation(Module::class.java)
        .forEach {
          val clazz = Class.forName(it)

          if (com.google.inject.Module::class.java.isAssignableFrom(clazz)) {
            val instance = clazz.newInstance() as com.google.inject.Module
            install(instance)
          } else {
            // Error
          }
        }
  }
}