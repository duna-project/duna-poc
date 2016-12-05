package io.duna.core.bootstrap

import co.paralleluniverse.fibers.instrument.JavaAgent
import com.ea.agentloader.AgentLoader
import com.ea.agentloader.ClassPathUtils
import io.duna.agent.DunaJavaAgent

object JavaAgentsLoader {

  /**
   * Loads the Quasar and Duna java agents if they're not active
   */
  fun attachRequiredJavaAgents() {
    if (!DunaJavaAgent.isActive()) {
      if (DunaJavaAgent::class.java.classLoader != ClassLoader.getSystemClassLoader()) {
        ClassPathUtils.appendToSystemPath(ClassPathUtils.getClassPathFor(DunaJavaAgent::class.java))
      }

      AgentLoader.loadAgentClass(DunaJavaAgent::class.java.name, null, null, true, true, false)
    }

    if (!JavaAgent.isActive()) {
      if (JavaAgent::class.java.classLoader != ClassLoader.getSystemClassLoader()) {
        ClassPathUtils.appendToSystemPath(ClassPathUtils.getClassPathFor(JavaAgent::class.java))
      }

      AgentLoader.loadAgentClass(JavaAgent::class.java.name, null, null, true, true, false)
    }
  }
}