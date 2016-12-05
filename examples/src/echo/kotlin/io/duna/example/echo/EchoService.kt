package io.duna.example.echo

import io.duna.core.service.Contract

@Contract
interface EchoService {
  fun echo(shout: String): String
}