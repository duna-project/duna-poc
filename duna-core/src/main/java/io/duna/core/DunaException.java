/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core;

/**
 * Created by carlos on 26/10/16.
 */
public class DunaException extends RuntimeException {
    public DunaException(Throwable t) {
        super(t);
    }

    public DunaException(String msg) {
        super(msg);
    }
}
