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

@Service
class EchoServiceImpl : EchoService {
  override fun echo(shout: String): String {
    return shout
  }
}
