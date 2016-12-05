package io.duna.example.impl

import io.duna.core.service.Service
import io.duna.example.EchoService

@Service
class EchoServiceImpl : EchoService {
  override fun echo(shout: String): String {
    return shout
  }
}