/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.persistence.jinq;

import org.jinq.jpa.JinqJPAStreamProvider;
import org.jinq.orm.stream.JinqStream;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;

public class JinqJpaStreamProvider implements JinqStreamProvider {

    private EntityManagerFactory entityManagerFactory;

    private JinqJPAStreamProvider jpaStreamProvider;

    @Inject
    public JinqJpaStreamProvider(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        this.jpaStreamProvider = new JinqJPAStreamProvider(entityManagerFactory);
    }

    @Override
    public <T> JinqStream<T> get(Class<T> entityClass) {
        return jpaStreamProvider.streamAll(entityManagerFactory.createEntityManager(),
            entityClass);
    }
}
