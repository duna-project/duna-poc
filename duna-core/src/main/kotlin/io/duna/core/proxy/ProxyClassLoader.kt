package io.duna.core.proxy

import io.duna.core.service.Contract
import java.lang.reflect.Modifier

class ProxyClassLoader(parent: ClassLoader,
                       private val proxyFactory: ServiceProxyFactory) : ClassLoader(parent) {

  fun proxyForService(serviceClass: Class<*>): Class<*> {
    if (!serviceClass.isAnnotationPresent(Contract::class.java)) {
      throw RuntimeException("[ERR-0001] ${serviceClass.canonicalName} isn't a valid service contract.")
    }

    if (!(serviceClass.isInterface || Modifier.isAbstract(serviceClass.modifiers))) {
      throw RuntimeException("[ERR-0002] Error registering ${serviceClass.canonicalName}: cannot register concrete classes " +
          "as remote services.")
    }

    val bytes = proxyFactory.generateFor(serviceClass)

    val clazz = defineClass(proxyFactory.namingStrategy.getProxyClassName(serviceClass),
        bytes, 0, bytes.size)
    resolveClass(clazz)

    return clazz
  }
}