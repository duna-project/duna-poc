package io.duna.core.proxy_gen.internal

import io.duna.core.proxy_gen.ServiceProxyNamingStrategy

class DefaultServiceProxyNamingStrategy : ServiceProxyNamingStrategy {
  override fun getProxyClassName(serviceClass: Class<*>): String {
    return "${serviceClass.canonicalName}\$RemoteProxy"
  }
}