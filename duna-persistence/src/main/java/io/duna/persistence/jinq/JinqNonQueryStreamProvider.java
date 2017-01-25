/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.persistence.jinq;

import org.jinq.orm.stream.JinqStream;
import org.jinq.orm.stream.NonQueryJinqStream;

import java.util.stream.Stream;

public class JinqNonQueryStreamProvider<T> implements JinqStreamProvider {

     private Stream<T> data;

    public JinqNonQueryStreamProvider(Stream<T> data) {
        this.data = data;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> JinqStream<V> get(Class<V> entityClass) {
        return new NonQueryJinqStream<>((Stream<V>) data);
    }

    @Override
    public void close(JinqStream<?> stream) {
    }
}
