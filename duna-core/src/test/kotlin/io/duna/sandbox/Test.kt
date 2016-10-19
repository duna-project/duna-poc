package io.duna.sandbox

import com.fasterxml.jackson.databind.ObjectMapper
import io.duna.asm.Type
import io.vertx.ext.sync.Sync
import java.lang.reflect.ParameterizedType
import java.util.*
import java.util.function.Consumer

object Test {

  @JvmStatic
  fun main(vararg args: String) {
    val json = """
      [[1,2,3,4],["asd","asdasd","aaa","aassdd"],[{"id": 1, "text": "asd"}]]
    """

    val objectMapper = ObjectMapper()
    val parser = objectMapper.factory.createParser(json)

    parser.nextToken()

    parser.nextValue()
    println(parser.readValueAs(List::class.java))

    parser.nextValue()
    println(parser.readValueAs(List::class.java))

    parser.nextValue()
    parser.nextToken()
    println(parser.readValuesAs(POJO::class.java).next().text)
  }
}