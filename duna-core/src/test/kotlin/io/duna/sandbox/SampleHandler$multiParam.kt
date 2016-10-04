package io.duna.sandbox

import com.google.inject.Inject
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer

class `SampleHandler$multiParam` : Handler<Buffer> {

  @Inject
  lateinit var service: SampleService

  override fun handle(event: Buffer?) {

  }
}