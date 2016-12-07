package io.duna.core.proxy

import io.duna.core.DunaException
import io.duna.core.service.Contract
import io.vertx.core.json.Json
import spock.lang.Shared
import spock.lang.Specification

import java.util.logging.Logger

/**
 * Evaluates the correctness of the proxies behaviour.
 */
class RemoteServiceProxyFactorySpec extends Specification {

  @Shared
  RemoteServiceProxyFactory proxyFactory = new RemoteServiceProxyFactory()

  def "Creating a proxy and trying to invoke a method"() {
    setup:
      def proxyClass = proxyFactory.loadProxyForService(ContractInterface)

    when:
      def proxy = proxyClass.newInstance()

    then:
      proxy instanceof ContractInterface &&
        proxy.hasProperty("logger") &&
        proxy.hasProperty("objectMapper")

    and:
    when:
      proxy.logger = Logger.anonymousLogger
      proxy.objectMapper = Json.mapper
      proxy.someMethod()

    then:
      thrown DunaException
  }

  def "Fail while trying to create a proxy for a non-contract interface"() {
    when:
      proxyFactory.loadProxyForService(NonContractInterface)

    then:
      thrown IllegalArgumentException
  }

  def "Fail while trying to create a proxy for a class"() {
    when:
      proxyFactory.loadProxyForService(NonContractInterface)

    then:
      thrown IllegalArgumentException
  }
}

@Contract
interface ContractInterface {
  def someMethod()
}

interface NonContractInterface {}

@Contract
class ContractClass {}