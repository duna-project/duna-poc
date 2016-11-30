package com.google.inject

import java.lang.reflect.Type

class UnsafeTypeLiteral(type: Type) : TypeLiteral<Any>(type) {
}