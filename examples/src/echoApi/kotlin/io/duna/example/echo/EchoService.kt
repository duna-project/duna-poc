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
import io.duna.http.HttpPath
import javax.inject.Qualifier

@Contract
@HttpPath("/echo")
interface EchoService {

  @HttpInterfaces(
    HttpInterface(method = HttpMethod.GET, path = "/:shout"),
    HttpInterface(method = HttpMethod.POST, path = "/echo/")
  )
  fun echo(shout: String): String
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class QualifiedService
