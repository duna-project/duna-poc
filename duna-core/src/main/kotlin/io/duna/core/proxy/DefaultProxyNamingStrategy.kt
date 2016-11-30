package io.duna.core.proxy

import java.util.concurrent.atomic.AtomicInteger

internal class DefaultProxyNamingStrategy : ProxyNamingStrategy {

  val proxyCounter = AtomicInteger(1)

  override fun getProxyName(contract: Class<*>): String =
      "${contract.name}\$Proxy${proxyCounter.getAndIncrement()}"
}