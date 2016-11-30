package io.duna.core.util

import java.lang.reflect.Method

/**
 *
 */
object Primitives {

  @JvmStatic
  fun getWrapper(primitiveClass: Class<*>): Class<*> {
    if (!primitiveClass.isPrimitive)
      throw IllegalArgumentException("The parameter must be a primitive type.")

    return primitiveClass.kotlin.javaObjectType
  }

  @JvmStatic
  fun getPrimitive(wrapperClass: Class<*>): Class<*> {
    return wrapperClass.kotlin.javaPrimitiveType ?:
        throw IllegalArgumentException("The parameter must be a wrapper for a primitive type.")
  }

  @JvmStatic
  fun getWrapperValueMethod(primitiveClass: Class<*>): Method {
    if (!primitiveClass.isPrimitive)
      throw IllegalArgumentException("The parameter must be a primitive type.")

    return primitiveClass.kotlin.javaObjectType.getMethod("valueOf", primitiveClass)
  }

  @JvmStatic
  fun getPrimitiveValueMethod(wrapperClass: Class<*>): Method {
    val primitive = wrapperClass.kotlin.javaPrimitiveType ?:
        throw IllegalArgumentException("The parameter must be a wrapper for a primitive type.")

    if (primitive.canonicalName == "void")
      throw IllegalArgumentException("Void can't hold a value.")

    return wrapperClass.kotlin.javaObjectType.getMethod("${primitive.canonicalName}Value")
  }
}