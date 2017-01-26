/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.persistence.jinq;

import org.jinq.orm.stream.InQueryStreamSource;
import org.jinq.orm.stream.JinqStream;
import org.jinq.orm.stream.NonQueryJinqStream;

import java.util.Map;
import java.util.stream.Stream;

public class JinqNonQueryStreamProvider implements JinqStreamProvider, InQueryStreamSource {

     private Map<Class<?>, Stream<?>> data;

    public JinqNonQueryStreamProvider(Map<Class<?>, Stream<?>> data) {
        this.data = data;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> JinqStream<V> get(Class<V> entityClass) {
        return new NonQueryJinqStream<>((Stream<V>) data.get(entityClass), this);
    }

    @Override
    public void close(JinqStream<?> stream) {
        stream.close();
    }

    @Override
    public <U> JinqStream<U> stream(Class<U> entityClass) {
        return get(entityClass);
    }
}
