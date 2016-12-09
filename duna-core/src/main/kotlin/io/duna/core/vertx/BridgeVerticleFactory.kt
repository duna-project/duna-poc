package io.duna.core.vertx

import com.google.inject.Injector
import io.vertx.core.Verticle
import io.vertx.core.spi.VerticleFactory
import java.util.logging.Logger
import javax.inject.Inject

class BridgeVerticleFactory : VerticleFactory {

  @Inject
  lateinit var injector: Injector

  @Inject
  lateinit var logger: Logger

  override fun createVerticle(verticleName: String?, classLoader: ClassLoader?): Verticle {
    val verticleClass = classLoader?.loadClass(VerticleFactory.removePrefix(verticleName))

    logger.fine { "Creating verticle for bridge ${VerticleFactory.removePrefix(verticleName)}" }

    val instance = verticleClass?.newInstance() as Verticle
    injector.injectMembers(instance)

    return instance
  }

  override fun blockingCreate(): Boolean = true

  override fun order(): Int = 1

  override fun prefix(): String = "duna-bridge"
}
