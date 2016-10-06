package io.duna.sandbox

import io.duna.asm.Type
import java.util.*

object Test {

  @JvmStatic
  fun main(vararg args: String) {
    println(Type.getDescriptor(Int::class.java))
    println(Type.getDescriptor(String::class.java))
    println(Type.getDescriptor(Any::class.java))
    println(Type.getDescriptor(Void::class.java))

    val t = LinkedList<Map<String, Int>>()

    val type = Type.getType(SampleService::class.java.methods[0])

    println(Type.getDescriptor(t.javaClass))
    println(type)
  }
}