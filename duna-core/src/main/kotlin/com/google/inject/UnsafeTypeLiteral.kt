package com.google.inject

import java.lang.reflect.Type

internal class UnsafeTypeLiteral(type: Type) : TypeLiteral<Any>(type)