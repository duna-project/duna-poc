package io.duna.core.service.invocation

interface ServiceInvocationHandler {
  fun call(service: Any, args: Array<Any>): Any
}