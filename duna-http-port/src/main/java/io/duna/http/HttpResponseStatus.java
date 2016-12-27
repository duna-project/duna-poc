/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.http;

public enum HttpResponseStatus {
    BAD_REQUEST(400),
    INTERNAL_SERVER_ERROR(500);

    int code;

    HttpResponseStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
