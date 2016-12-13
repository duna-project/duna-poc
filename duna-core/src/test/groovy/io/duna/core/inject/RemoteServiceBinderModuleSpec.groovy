/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.inject

import com.google.inject.ConfigurationException
import com.google.inject.Guice
import io.duna.core.inject.service.RemoteServiceBinderModule
import io.duna.core.proxy.GeneratedProxy
import spock.lang.Shared
import spock.lang.Specification

/**
 * Ensures that only remote proxies are injected by this module.
 */
class RemoteServiceBinderModuleSpec extends Specification {
  @Shared injector = Guice.createInjector(RemoteServiceBinderModule.INSTANCE)

  def "Injecting a remote service proxy"() {
    setup:
      def dependant = new DependsOnRemoteService()

    when:
      injector.injectMembers(dependant)

    then:
      dependant.remoteService != null &&
        dependant.remoteService.class.isAnnotationPresent(GeneratedProxy)
        dependant.remoteService instanceof RemoteService
  }

  def "Shouldn't injecting local implementations nor create proxies for local services"() {
    setup:
      def dependant = new DependsOnLocalService()

    when:
      injector.injectMembers(dependant)

    then:
      thrown ConfigurationException
  }
}
