package io.duna.core.proxy.internal

import io.duna.core.proxy.ServiceProxyNamingStrategy

class DefaultServiceProxyNamingStrategy : ServiceProxyNamingStrategy {
  override fun getProxyClassName(serviceClass: Class<*>): String {
    return "${serviceClass.canonicalName}\$RemoteProxy"
  }
}