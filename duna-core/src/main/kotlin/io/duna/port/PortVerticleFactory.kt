package io.duna.port

import com.google.inject.Injector
import io.vertx.core.Verticle
import io.vertx.core.spi.VerticleFactory
import java.util.logging.Logger
import javax.inject.Inject

internal class PortVerticleFactory
  @Inject
  constructor(private val injector: Injector,
              private val logger: Logger) : VerticleFactory {

  override fun createVerticle(verticleName: String?, classLoader: ClassLoader?): Verticle {
    val verticleClass = classLoader?.loadClass(VerticleFactory.removePrefix(verticleName))

    if (!Verticle::class.java.isAssignableFrom(verticleClass)) {
      logger.severe { "${VerticleFactory.removePrefix(verticleName)} is not a valid verticle identifier." }
      throw IllegalArgumentException("$verticleName isn't a valid Verticle implementation")
    }

    logger.fine { "Creating verticle for port ${VerticleFactory.removePrefix(verticleName)}" }

    return injector.getBinding(verticleClass).provider.get() as Verticle
  }

  override fun blockingCreate(): Boolean = true

  override fun order(): Int = 1

  override fun prefix(): String = "duna-port"
}
