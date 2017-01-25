/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.persistence.jinq;

import org.jinq.jpa.JPAJinqStream;
import org.jinq.jpa.JinqJPAStreamProvider;
import org.jinq.orm.internal.QueryComposer;
import org.jinq.orm.stream.JinqStream;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JinqJpaStreamProvider implements JinqStreamProvider {

    private EntityManagerFactory entityManagerFactory;

    private JinqJPAStreamProvider jpaStreamProvider;

    private Logger logger;

    private Field jpaComposerField;

    private Field entityManagerField;

    @Inject
    public JinqJpaStreamProvider(EntityManagerFactory entityManagerFactory, Logger logger) {
        this.entityManagerFactory = entityManagerFactory;
        this.jpaStreamProvider = new JinqJPAStreamProvider(entityManagerFactory);
        this.logger = logger;

        try {
            this.jpaComposerField = Class
                .forName("org.jinq.jpa.QueryJPAJinqStream")
                .getDeclaredField("jpaComposer");
            this.jpaComposerField.setAccessible(true);

            this.entityManagerField = Class
                .forName("org.jinq.jpa.JPAQueryComposer")
                .getDeclaredField("em");
            this.entityManagerField.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchFieldException ex) {
            logger.log(Level.SEVERE, ex, () -> "Error while trying to get stream fields:");
        }
    }

    @Override
    public <T> JinqStream<T> get(Class<T> entityClass) {
        return jpaStreamProvider.streamAll(entityManagerFactory.createEntityManager(),
            entityClass);
    }

    @Override
    public void close(JinqStream<?> stream) {
        if (!(stream instanceof JPAJinqStream)) {
            throw new RuntimeException("The stream provided isn't a JPAJinqStream.");
        }

        try {
            QueryComposer<?> composer = (QueryComposer<?>) jpaComposerField.get(stream);

            if (composer == null)
                throw new RuntimeException("Composer not found");

            EntityManager entityManager = (EntityManager) entityManagerField.get(composer);
            entityManager.close();
        } catch (IllegalAccessException ignored) {
            ignored.printStackTrace();
        }
    }
}
