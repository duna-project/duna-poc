package io.duna.core.service.impl

import co.paralleluniverse.fibers.Suspendable
import com.fasterxml.jackson.core.JsonToken
import io.duna.core.io.BufferInputStream
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.Message
import io.vertx.core.json.Json

class GenericActionHandler : Handler<Message<Buffer>> {

  @Suspendable
  override fun handle(event: Message<Buffer>) {
    val inBuffer = BufferInputStream(event.body())
    val parser = Json.mapper.factory.createParser(inBuffer)

    parser.nextToken() // START_OBJECT
    while (parser.nextToken() != JsonToken.END_OBJECT) {
      val fieldId = parser.currentName

      val token = parser.nextToken()
      val result = when (token) {
        JsonToken.VALUE_STRING -> parser.valueAsString
        JsonToken.VALUE_NUMBER_INT -> parser.valueAsLong
        JsonToken.VALUE_NUMBER_FLOAT -> parser.valueAsDouble
//        JsonToken.START_OBJECT -> parser.readValueAs()
        else -> Unit
      }
    }
    parser.nextToken()
//    val paramNode = jsonTree.path("a")


  }
}