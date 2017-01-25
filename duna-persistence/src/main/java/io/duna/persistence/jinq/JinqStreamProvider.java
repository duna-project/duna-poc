/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.persistence.jinq;

import org.jinq.orm.stream.JinqStream;

public interface JinqStreamProvider {
    <T> JinqStream<T> get(Class<T> entityClass);

    void close(JinqStream<?> stream);
}
