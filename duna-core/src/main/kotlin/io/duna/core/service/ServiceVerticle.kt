package io.duna.core.service

import com.google.inject.Inject
import com.google.inject.Injector
import io.vertx.ext.sync.SyncVerticle

class ServiceVerticle(val serviceClass: Class<*>) : SyncVerticle() {

  @Inject
  lateinit var injector: Injector

  var service: Any? = null

  override fun start() {
    service = injector.getInstance(serviceClass)

    if (service == null || service is ServiceProxy) {
      throw Exception("Cannot create verticles for service ${serviceClass}. " +
          "No implementations found.")
    }

    if (!serviceClass.isInterface) {
      throw Exception("The service class must be an interface.")
    }

    serviceClass.methods.forEach { method ->

    }
  }
}