package io.duna.core.service.handlers

import spock.lang.Specification

/**
 * Ensures the correctness of the method call delegation. Maybe
 * this should be placed under the MethodCallDemuxing responsibility.
 */
class GenericActionHandlerSpec extends Specification {

  def "Forwarding a method invocation to an implementation"() {
    setup:
      def echo = new Echo()
      def target = new GenericActionHandler(echo, echo.class.getMethod("shout", String))

    when:
      def result = target.serviceCallDelegation.invoke(echo, "Yoleihu!")

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