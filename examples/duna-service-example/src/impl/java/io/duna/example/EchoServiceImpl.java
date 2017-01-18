/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.example;

import io.duna.core.service.Service;
import io.duna.http.Parameter;

@Service
public class EchoServiceImpl implements EchoService {

    /*
     * The @Parameter annotation is optional when compiled with Java 8's argument -parameter
     */
    @Override
    public String echo(@Parameter("shout") String shout) {
        return shout;
    }
}
