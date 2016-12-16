/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.example.echo.impl

import io.duna.core.service.Service
import io.duna.example.echo.EchoService
import io.duna.http.HttpMethod
import io.duna.http.HttpPort
import io.duna.http.Verb

@Service
@HttpPort
open class EchoServiceImpl : EchoService {

  @HttpMethod(Verb.GET)
  override fun echo(shout: String, myAsf: Int): String {
    return shout
  }
}
