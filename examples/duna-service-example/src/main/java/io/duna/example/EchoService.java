/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.example;

import io.duna.core.service.Contract;
import io.duna.http.HttpInterface;
import io.duna.http.HttpPath;

import static io.duna.http.HttpMethod.POST;

@Contract
@HttpPath("/echoService")
public interface EchoService {

    @HttpInterface(path = "/echo")
    @HttpInterface(method = POST, path = "/echo")
    String echo(String shout);
}
