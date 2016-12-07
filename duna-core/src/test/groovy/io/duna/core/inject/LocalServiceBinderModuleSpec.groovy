package io.duna.core.inject

import com.google.inject.ConfigurationException
import com.google.inject.Guice
import spock.lang.Shared
import spock.lang.Specification

/**
 * Ensures that only local implementations are injected by
 * this module.
 */
class LocalServiceBinderModuleSpec extends Specification {
  @Shared injector = Guice.createInjector(LocalServiceBinderModule.INSTANCE)

  def "Injecting the local service implementation"() {
    setup:
      def dependant = new DependsOnLocalService()

    when:
      injector.injectMembers(dependant)

    then:
      dependant.localService != null &&
        dependant.localService instanceof LocalService
  }

  def "Shouldn't inject remote service proxies nor non-service implementations"() {
    setup:
      def dependant = new DependsOnRemoteService()

    when:
      injector.injectMembers(dependant)

    then:
      thrown ConfigurationException
  }
}
