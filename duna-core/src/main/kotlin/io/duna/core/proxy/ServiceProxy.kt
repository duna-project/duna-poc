package io.duna.core.proxy

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.Vertx

internal interface ServiceProxy {
  fun getVertx(): Vertx
  fun getObjectMapper(): ObjectMapper
}