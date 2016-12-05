package io.duna.example.echo.impl

import io.duna.core.service.Service
import io.duna.example.echo.EchoService

@Service
class EchoServiceImpl : EchoService {
  override fun echo(shout: String): String {
    return shout
  }
}