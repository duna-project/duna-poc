package io.duna.core.services

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult
import io.vertx.core.Verticle
import io.vertx.core.Vertx

class ServiceVerticleBinder(val scanResult: ScanResult) : AbstractModule() {

  override fun configure() {
    val multibinder = Multibinder.newSetBinder(binder(), Verticle::class.java)
    scanResult
        .getNamesOfClassesWithAnnotation(Bootstrap::class.java)
        .forEach {
          val clazz = Class.forName(it)

          if (Verticle::class.java.isAssignableFrom(clazz)) {
            multibinder.addBinding().to(clazz)
          } else {
            // Error
          }
        }
  }
}