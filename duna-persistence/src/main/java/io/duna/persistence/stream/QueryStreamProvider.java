/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.persistence.stream;

/**
 * Created by carlos on 31/01/17.
 */
public interface QueryStreamProvider {
    void registerCustomTuple(Class<?> tupleType);

    <T> QueryStream<T> stream(Class<T> entityClass);

    <T> QueryStream<T> stream(Class<T> entityClass, QueryStream<?> sourceStream);
}
