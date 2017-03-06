/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package org.jinq.jpa;

import io.duna.persistence.jpa.JPAQueryStream;
import io.duna.persistence.stream.QueryStream;
import io.duna.persistence.stream.QueryStreamProvider;

import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.orm.stream.InQueryStreamSource;
import org.jinq.orm.stream.JinqStream;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Transient;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JPAQueryStreamProvider extends JinqJPAStreamProvider implements QueryStreamProvider {

    private final Map<EntityManager, AtomicInteger> references;

    private final EntityManagerFactory entityManagerFactory;

    private final Logger logger;

    @Inject
    public JPAQueryStreamProvider(EntityManagerFactory factory, Logger logger) {
        super(factory);

        this.entityManagerFactory = factory;
        this.logger = logger;
        this.references = new ConcurrentHashMap<>();
    }

    @Override
    public void registerCustomTuple(Class<?> tupleType) {
        List<Method> getters = Stream.of(tupleType.getDeclaredMethods())
            .filter(m -> !m.isAnnotationPresent(Transient.class)
                && m.getName().startsWith("get")
                && m.getParameterCount() == 0)
            .collect(Collectors.toList());

        Method[] getterArray = new Method[getters.size()];
        getters.toArray(getterArray);

        for (Constructor constructor : tupleType.getConstructors()) {
            metamodel.insertCustomTupleConstructor(tupleType.getName(),
                constructor, getterArray);
        }
    }

    @Override
    public <T> QueryStream<T> stream(Class<T> entityClass) {
        return streamAll(entityManagerFactory.createEntityManager(), entityClass);
    }

    @Override
    public <U> JPAQueryStream<U> streamAll(EntityManager em, Class<U> entity) {
        String entityName = metamodel.entityNameFromClass(entity);
        Optional<JPQLQuery<?>> cachedQuery = hints.useCaching
            ? cachedQueries.findCachedFindAllEntities(entityName)
            : null;

        if (cachedQuery == null) {
            JPQLQuery<U> query = JPQLQuery.findAllEntities(entityName);
            cachedQuery = Optional.of(query);
            if (hints.useCaching)
                cachedQuery = cachedQueries.cacheFindAllEntities(entityName, cachedQuery);
        }

        @SuppressWarnings({"unchecked", "OptionalGetWithoutIsPresent"})
        JPQLQuery<U> query = (JPQLQuery<U>) cachedQuery.get();

        return new PersistableJPAQueryStream<>(JPAQueryComposer.findAllEntities(
            metamodel, cachedQueries, lambdaAnalyzer, jpqlQueryTransformConfigurationFactory,
            em, hints, query),

            new InQueryStreamSource() {
                @Override
                public <S> JinqStream<S> stream(Class<S> entityClass) {
                    return streamAll(em, entityClass);
                }
            });
    }

    public <T> QueryStream<T> stream(Class<T> entityClass, QueryStream<?> source) {
        if(!(source instanceof JPAQueryStream)) {
            throw new IllegalArgumentException("Source must be a JPA stream.");
        } else {
            if (source instanceof PersistableJPAQueryStream) {
                return this.streamAll(((PersistableJPAQueryStream)source).jpaComposer.em, entityClass);
            } else if (source instanceof QueryJPAJinqStream) {
                return this.streamAll(((QueryJPAJinqStream)source).jpaComposer.em, entityClass);
            } else {
                return this.stream(entityClass);
            }
        }
    }
}
