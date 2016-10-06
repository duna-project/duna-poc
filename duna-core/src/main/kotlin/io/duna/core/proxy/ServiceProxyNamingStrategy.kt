package io.duna.core.proxy

interface ServiceProxyNamingStrategy {

  fun getProxyClassName(serviceClass: Class<*>): String
}