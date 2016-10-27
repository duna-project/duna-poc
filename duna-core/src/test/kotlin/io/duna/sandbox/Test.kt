package io.duna.sandbox

import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.ObjectMapper
import io.duna.asm.Type
import io.vertx.ext.sync.Sync
import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.implementation.MethodCall
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

object Target {

  fun targetMethod(str: String, int: Int, pojo: POJO) {
    println(str)
    println(int)
    println(pojo)
  }
}

object Test {

  @JvmStatic
  fun main(vararg args: String) {

    val paramsTypes = arrayOf(String::class.java, Int::class.java, POJO::class.java)
    val params = arrayOf<Any>("ASd", 1, POJO(1, "ASd"))

    val proxy = ByteBuddy()
    .subclass(Any::class.java)
    .implement(Invocation::class.java)
    .method(ElementMatchers.isDeclaredBy(Invocation::class.java))
    .intercept(
        MethodCall.invoke(
            Target::class.java.getDeclaredMethod("targetMethod", *paramsTypes))
        .on(Target)
        .withAllArguments()
    )
    .make()
    .load(ClassLoader.getSystemClassLoader())
    .loaded
    .newInstance() as Invocation

    proxy.invoke(*params)

//    val parser = io.vertx.core.json.Json.mapper.factory.createParser("""{
//      "field0": "asdsdasd",
//      "field2": [1,2,3,4,5],
//      "field1": {"asdasd": "asd"}
//      }""")
//
//    parser.nextToken()
//    while (parser.nextToken() != JsonToken.END_OBJECT) {
//      println(parser.currentToken)
//      println(parser.currentName)
//
//      parser.nextToken()
//      if (parser.currentToken == JsonToken.VALUE_STRING)
//        println(parser.readValueAs(String::class.java))
//
//      if (parser.currentToken == JsonToken.START_ARRAY) {
//        println("Array")
//        println(Arrays.toString(parser.readValueAs(Array<Int>::class.java)))
//      }
//    }


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