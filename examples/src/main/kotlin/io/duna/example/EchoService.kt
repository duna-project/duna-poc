package io.duna.example

import io.duna.core.service.Contract

@Contract
interface EchoService {
  fun echo(shout: String): String
}