/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.service.handler

import io.vertx.core.json.Json
import spock.lang.Specification

import java.util.logging.LogManager

/**
 * Ensures the correctness of the method call delegation. Maybe
 * this should be placed under the DestructuringMethodCall responsibility.
 */
class GenericActionHandlerSpec extends Specification {

  def "Forwarding a method invocation to an implementation"() {
    setup:
      def echo = new Echo()
      def target = new DefaultServiceHandler(echo,
        echo.class.getMethod("shout", String),
        Json.mapper,
        LogManager.getLogManager().getLogger(DefaultServiceHandler.class.name))

    when:
      def result = target.getServiceCallDelegator.invoke(echo, "Yoleihu!")

    then:
      noExceptionThrown()

    and:
        result == "Yoleihu!"
  }
}

class Echo {
  @SuppressWarnings("GroovyUnusedDeclaration")
  def shout(String message) { return message }
}
