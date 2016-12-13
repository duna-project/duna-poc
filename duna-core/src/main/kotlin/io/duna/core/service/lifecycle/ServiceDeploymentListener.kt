package io.duna.core.service.lifecycle

interface ServiceDeploymentListener {
  fun deployed()
  fun undeployed()
}
