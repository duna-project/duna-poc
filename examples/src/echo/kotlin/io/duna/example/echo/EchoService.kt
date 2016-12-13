/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.example.echo

import io.duna.core.service.Address
import io.duna.core.service.Contract
import io.duna.web.annotations.HttpInterface
import io.duna.web.annotations.HttpMethod

@Contract
@HttpInterface
interface EchoService {

  @HttpMethod(HttpMethod.Method.GET)
  fun echo(shout: String): String
}
