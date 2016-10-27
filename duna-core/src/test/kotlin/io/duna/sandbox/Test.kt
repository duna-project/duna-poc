package io.duna.sandbox

import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.ObjectMapper
import io.duna.asm.Type
import io.vertx.ext.sync.Sync
import net.bytebuddy.ByteBuddy
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.implementation.bind.annotation.Origin
import net.bytebuddy.matcher.ElementMatchers
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.util.*
import java.util.function.Consumer

interface Y {
  fun z()
}

object X {
  fun intercept(@Origin method: Method): String {
    println(method.declaringClass)

    return "sdfdsfg"
  }
}

object Test {

  @JvmStatic
  fun main(vararg args: String) {
    val parser = io.vertx.core.json.Json.mapper.factory.createParser("""{
      "field0": "asdsdasd",
      "field2": [1,2,3,4,5],
      "field1": {"asdasd": "asd"}
      }""")

    parser.nextToken()
    while (parser.nextToken() != JsonToken.END_OBJECT) {
      println(parser.currentToken)
      println(parser.currentName)

      parser.nextToken()
      if (parser.currentToken == JsonToken.VALUE_STRING)
        println(parser.readValueAs(String::class.java))

      if (parser.currentToken == JsonToken.START_ARRAY) {
        println("Array")
        println(Arrays.toString(parser.readValueAs(Array<Int>::class.java)))
      }
    }


//    val json = """
//      [[1,2,3,4],["asd","asdasd","aaa","aassdd"],[{"id": 1, "text": "asd"}]]
//    """
//
//    val objectMapper = ObjectMapper()
//    val parser = objectMapper.factory.createParser(json)
//
//    parser.nextToken()
//
//    parser.nextValue()
//    println(parser.readValueAs(List::class.java))
//
//    parser.nextValue()
//    println(parser.readValueAs(List::class.java))
//
//    parser.nextValue()
//    parser.nextToken()
//    println(parser.readValuesAs(POJO::class.java).next().text)
  }
}