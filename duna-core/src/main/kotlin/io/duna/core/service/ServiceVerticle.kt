package io.duna.core.service

import co.paralleluniverse.fibers.Suspendable
import com.google.inject.Injector
import io.vertx.core.eventbus.EventBus
import io.vertx.ext.sync.SyncVerticle
import javax.inject.Inject

/**
 * Service facade responsible to receiving [EventBus] events and
 * dispatching them to the corresponding service.
 */
class ServiceVerticle<T>(private val serviceClass: Class<T>) : SyncVerticle() {

  @Inject
  lateinit var injector: Injector

  var service: T? = null

  @Suspendable
  override fun start() {
    // Map all service methods and create handlers for it
    service = injector.getBinding(serviceClass).provider.get()
  }
}