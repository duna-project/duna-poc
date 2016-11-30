package io.duna.core.proxy

/**
 * Created by carlos on 28/11/16.
 */
interface ProxyNamingStrategy {
  fun getProxyName(contract: Class<*>): String
}