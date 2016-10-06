package io.duna.sandbox

import io.duna.asm.Type
import io.vertx.ext.sync.Sync
import java.lang.reflect.ParameterizedType
import java.util.*
import java.util.function.Consumer

object Test {

  @JvmStatic
  fun main(vararg args: String) {
    Sync::class.java.methods.forEach { println(it) }
    println(Sync::class.java.getMethod("awaitResult", Consumer::class.java))
  }
}