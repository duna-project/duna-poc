/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.persistence.stream;

import org.jinq.orm.stream.InQueryStreamSource;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class NonQueryStreamProvider implements QueryStreamProvider, InQueryStreamSource {

    private Map<Class<?>, Collection<?>> data;

    public NonQueryStreamProvider(Map<Class<?>, Collection<?>> data) {
        this.data = data;
    }


    @Override
    public void registerCustomTuple(Class<?> tupleType) {
        // NO-OP
    }

    @Override
    public <T> QueryStream<T> stream(Class<T> entityClass) {
        return new NonQueryStream<T>((Stream<T>) data.get(entityClass).stream(), this);
    }

    @Override
    public <T> QueryStream<T> stream(Class<T> entityClass, QueryStream<?> sourceStream) {
        return this.stream(entityClass);
    }

    @SuppressWarnings("unchecked")
    protected <T> Collection<T> getDataSource(Class<?> entityClass) {
        return (Collection<T>) data.getOrDefault(entityClass, ConcurrentHashMap.newKeySet());
    }
}
