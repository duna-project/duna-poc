package com.google.inject

import java.lang.reflect.Type

class ManualTypeLiteral(type: Type) : TypeLiteral<Any>(type) {
}