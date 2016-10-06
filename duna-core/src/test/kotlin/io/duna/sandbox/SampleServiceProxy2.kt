package io.duna.sandbox

import co.paralleluniverse.fibers.Suspendable
import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.Message
import io.vertx.ext.sync.Sync.awaitResult
import java.io.ByteArrayOutputStream
import javax.inject.Inject

//class SampleServiceProxy2(val address: String) : SampleService {
//
//  @Inject
//  private lateinit var vertx: Vertx
//
//  @Inject
//  private lateinit var objectMapper: ObjectMapper
//
//  @Suspendable
//  override fun call(m: String, n: POJO, z: Int): POJO {
//    val outStream = ByteArrayOutputStream()
//    val generator = objectMapper.factory.createGenerator(outStream)
//
//    generator.writeStartArray()
//
//    // Serialize parameters
//    generator.writeObject(m)
//    generator.writeObject(n)
//    generator.writeObject(z)
//
//    generator.writeEndArray()
//    generator.flush()
//
//    val result = awaitResult { h: Handler<AsyncResult<Message<Buffer>>> ->
//      vertx.eventBus().send(address, outStream.toByteArray(), h)
//    }
//
//    val parser = objectMapper.readerFor(POJO::class.java)
//    return parser.readValue(result.body().bytes)
//  }
//}