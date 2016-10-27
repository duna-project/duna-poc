package io.duna.core.proxy_gen

interface ServiceProxyNamingStrategy {

  fun getProxyClassName(serviceClass: Class<*>): String
}