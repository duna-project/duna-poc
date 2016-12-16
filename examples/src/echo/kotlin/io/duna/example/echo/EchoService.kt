/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.example.echo

import io.duna.core.service.Contract
import io.duna.http.HttpInterface
import io.duna.http.HttpInterfaces
import io.duna.http.HttpMethod
import io.duna.http.HttpPort

@Contract
@HttpPort
interface EchoService {

  @HttpInterfaces(
    HttpInterface(path = "/echo/:shout"),
    HttpInterface(method = HttpMethod.POST, path = "/echo/")
  )
  fun echo(shout: String, myAsf: Int): String
}
