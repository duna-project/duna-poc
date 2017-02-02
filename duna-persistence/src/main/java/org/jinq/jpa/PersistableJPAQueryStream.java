/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package org.jinq.jpa;

import io.duna.persistence.jpa.JPAQueryStream;
import org.jinq.orm.internal.QueryComposer;
import org.jinq.orm.stream.InQueryStreamSource;
import org.jinq.orm.stream.JinqStream;
import org.jinq.orm.stream.QueryJinqStream;
import org.jinq.tuples.*;

import java.util.function.BiConsumer;

public class PersistableJPAQueryStream<T> extends QueryJinqStream<T> implements JPAQueryStream<T> {

    final JPAQueryComposer<T> jpaComposer;

    public PersistableJPAQueryStream(QueryComposer<T> query) {
        super(query);

        if (!(query instanceof JPAQueryComposer))
            throw new IllegalArgumentException("Cannot make a JPA stream without a JPA Query Composer");

        jpaComposer = (JPAQueryComposer<T>) query;
    }

    public PersistableJPAQueryStream(QueryComposer<T> query,
                                     InQueryStreamSource inQueryStreamSource) {
        super(query, inQueryStreamSource);

        if (!(query instanceof JPAQueryComposer))
            throw new IllegalArgumentException("Cannot make a JPA stream without a JPA Query Composer");

        jpaComposer = (JPAQueryComposer<T>) query;
    }

    protected <U> JPAQueryStream<U> makeQueryStream(QueryComposer<U> query, InQueryStreamSource inQueryStreamSource) {
        return new PersistableJPAQueryStream<>(query, inQueryStreamSource);
    }

    InQueryStreamSource getInQueryStreamSource() {
        return this.inQueryStreamSource;
    }

    private <U> JPAQueryStream<U> wrap(JinqStream<U> toWrap) {
        if (toWrap instanceof JPAQueryStream) return (JPAQueryStream<U>) toWrap;
        if (toWrap instanceof QueryJinqStream)
            throw new IllegalArgumentException("Should not be possible to get a non-JPA JinqStream here");
        return new JPAQueryStreamWrapper<>(toWrap);
    }

    // New JPA-specific API
    @Override
    public <U> JPAQueryStream<T> joinFetch(
        org.jinq.orm.stream.JinqStream.Join<T, U> join) {
        QueryComposer<T> newComposer = jpaComposer.joinFetch(join);
        if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
        return new JPAQueryStreamWrapper<>(this).joinFetch(join);
    }

    @Override
    public <U> JPAQueryStream<T> joinFetchList(
        org.jinq.orm.stream.JinqStream.JoinToIterable<T, U> join) {
        QueryComposer<T> newComposer = jpaComposer.joinFetchIterable(join);
        if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
        return new JPAQueryStreamWrapper<>(this).joinFetchList(join);
    }

    @Override
    public <U> JPAQueryStream<T> leftOuterJoinFetch(
        org.jinq.orm.stream.JinqStream.Join<T, U> join) {
        QueryComposer<T> newComposer = jpaComposer.leftOuterJoinFetch(join);
        if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
        return new JPAQueryStreamWrapper<>(this).leftOuterJoinFetch(join);
    }

    @Override
    public <U> JPAQueryStream<T> leftOuterJoinFetchList(
        org.jinq.orm.stream.JinqStream.JoinToIterable<T, U> join) {
        QueryComposer<T> newComposer = jpaComposer.leftOuterJoinFetchIterable(join);
        if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
        return new JPAQueryStreamWrapper<>(this).leftOuterJoinFetchList(join);
    }

    @Override
    public JPAQueryStream<T> orUnion(JPAJinqStream<T> otherSet) {
        QueryComposer<T> newComposer = jpaComposer.orUnion(otherSet);
        if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
        return new JPAQueryStreamWrapper<>(this).orUnion(otherSet);
    }

    @Override
    public JPAQueryStream<T> andIntersect(JPAJinqStream<T> otherSet) {
        QueryComposer<T> newComposer = jpaComposer.andIntersect(otherSet);
        if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
        return new JPAQueryStreamWrapper<>(this).andIntersect(otherSet);
    }


    // Wrapped versions of old API

    @Override
    public <E extends Exception> JPAQueryStream<T> where(
        org.jinq.orm.stream.JinqStream.Where<T, E> test) {
        return wrap(super.where(test));
    }

    @Override
    public <E extends Exception> JPAQueryStream<T> where(
        org.jinq.orm.stream.JinqStream.WhereWithSource<T, E> test) {
        return wrap(super.where(test));
    }

    @Override
    public <U> JPAQueryStream<U> select(
        org.jinq.orm.stream.JinqStream.Select<T, U> select) {
        return wrap(super.select(select));
    }

    @Override
    public <U> JPAQueryStream<U> select(
        org.jinq.orm.stream.JinqStream.SelectWithSource<T, U> select) {
        return wrap(super.select(select));
    }

    @Override
    public <U> JPAQueryStream<U> selectAll(
        org.jinq.orm.stream.JinqStream.Join<T, U> select) {
        return wrap(super.selectAll(select));
    }

    @Override
    public <U> JPAQueryStream<U> selectAll(
        org.jinq.orm.stream.JinqStream.JoinWithSource<T, U> select) {
        return wrap(super.selectAll(select));
    }

    @Override
    public <U> JPAQueryStream<U> selectAllList(
        org.jinq.orm.stream.JinqStream.JoinToIterable<T, U> select) {
        return wrap(super.selectAllList(select));
    }

    @Override
    public <U> JPAQueryStream<Pair<T, U>> join(
        org.jinq.orm.stream.JinqStream.Join<T, U> join) {
        return wrap(super.join(join));
    }

    @Override
    public <U> JPAQueryStream<Pair<T, U>> join(
        org.jinq.orm.stream.JinqStream.JoinWithSource<T, U> join) {
        return wrap(super.join(join));
    }

    @Override
    public <U> JPAQueryStream<Pair<T, U>> joinList(
        org.jinq.orm.stream.JinqStream.JoinToIterable<T, U> join) {
        return wrap(super.joinList(join));
    }

    @Override
    public <U> JPAQueryStream<Pair<T, U>> leftOuterJoin(
        org.jinq.orm.stream.JinqStream.Join<T, U> join) {
        return wrap(super.leftOuterJoin(join));
    }

    @Override
    public <U> JPAQueryStream<Pair<T, U>> leftOuterJoinList(
        org.jinq.orm.stream.JinqStream.JoinToIterable<T, U> join) {
        return wrap(super.leftOuterJoinList(join));
    }

    @Override
    public <U> JPAQueryStream<Pair<T, U>> leftOuterJoin(
        org.jinq.orm.stream.JinqStream.JoinWithSource<T, U> join,
        org.jinq.orm.stream.JinqStream.WhereForOn<T, U> on) {
        return wrap(super.leftOuterJoin(join, on));
    }

    @Override
    public <U> JPAQueryStream<Pair<T, U>> crossJoin(JinqStream<U> join) {
        return wrap(super.crossJoin(join));
    }


    @Override
    public <U, V> JPAQueryStream<Pair<U, V>> group(
        org.jinq.orm.stream.JinqStream.Select<T, U> select,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, V> aggregate) {
        return wrap(super.group(select, aggregate));
    }

    @Override
    public <U, V, W> JPAQueryStream<Tuple3<U, V, W>> group(
        org.jinq.orm.stream.JinqStream.Select<T, U> select,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, V> aggregate1,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, W> aggregate2) {
        return wrap(super.group(select, aggregate1, aggregate2));
    }

    @Override
    public <U, V, W, X> JPAQueryStream<Tuple4<U, V, W, X>> group(
        org.jinq.orm.stream.JinqStream.Select<T, U> select,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, V> aggregate1,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, W> aggregate2,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, X> aggregate3) {
        return wrap(super.group(select, aggregate1, aggregate2, aggregate3));
    }

    @Override
    public <U, V, W, X, Y> JPAQueryStream<Tuple5<U, V, W, X, Y>> group(
        org.jinq.orm.stream.JinqStream.Select<T, U> select,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, V> aggregate1,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, W> aggregate2,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, X> aggregate3,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, Y> aggregate4) {
        return wrap(super.group(select, aggregate1, aggregate2, aggregate3, aggregate4));
    }

    @Override
    public <U, V, W, X, Y, Z> JPAQueryStream<Tuple6<U, V, W, X, Y, Z>> group(
        org.jinq.orm.stream.JinqStream.Select<T, U> select,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, V> aggregate1,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, W> aggregate2,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, X> aggregate3,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, Y> aggregate4,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, Z> aggregate5) {
        return wrap(super.group(select, aggregate1, aggregate2, aggregate3,
                                aggregate4, aggregate5));
    }

    @Override
    public <U, V, W, X, Y, Z, A> JPAQueryStream<Tuple7<U, V, W, X, Y, Z, A>> group(
        org.jinq.orm.stream.JinqStream.Select<T, U> select,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, V> aggregate1,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, W> aggregate2,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, X> aggregate3,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, Y> aggregate4,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, Z> aggregate5,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, A> aggregate6) {
        return wrap(super.group(select, aggregate1, aggregate2, aggregate3,
                                aggregate4, aggregate5, aggregate6));
    }

    @Override
    public <U, V, W, X, Y, Z, A, B> JPAQueryStream<Tuple8<U, V, W, X, Y, Z, A, B>> group(
        org.jinq.orm.stream.JinqStream.Select<T, U> select,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, V> aggregate1,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, W> aggregate2,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, X> aggregate3,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, Y> aggregate4,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, Z> aggregate5,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, A> aggregate6,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, B> aggregate7) {
        return wrap(super.group(select, aggregate1, aggregate2, aggregate3,
                                aggregate4, aggregate5, aggregate6, aggregate7));
    }

    @Override
    public <V extends Comparable<V>> JPAQueryStream<T> sortedBy(
        org.jinq.orm.stream.JinqStream.CollectComparable<T, V> sortField) {
        return wrap(super.sortedBy(sortField));
    }

    @Override
    public <V extends Comparable<V>> JPAQueryStream<T> sortedDescendingBy(
        org.jinq.orm.stream.JinqStream.CollectComparable<T, V> sortField) {
        return wrap(super.sortedDescendingBy(sortField));
    }

    @Override
    public JPAQueryStream<T> skip(long n) {
        return wrap(super.skip(n));
    }

    @Override
    public JPAQueryStream<T> limit(long n) {
        return wrap(super.limit(n));
    }

    @Override
    public JPAQueryStream<T> distinct() {
        return wrap(super.distinct());
    }

    @Override
    public JPAQueryStream<T> setHint(String name, Object value) {
        return wrap(super.setHint(name, value));
    }

    @Override
    public void begin() {
        jpaComposer.em.getTransaction().begin();
    }

    @Override
    public void commit() {
        jpaComposer.em.getTransaction().commit();
    }

    @Override
    public <V> V merge(V entity) {
        try {
            return jpaComposer.em.merge(entity);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }

        return entity;
    }

    @Override
    public void merge() {
        this.forEach(this::merge);
    }

    @Override
    public void flush() {
        jpaComposer.em.flush();
    }

    @Override
    public void remove(Object entity) {
        jpaComposer.em.remove(entity);
    }

    @Override
    public void persist(Object entity) {
        jpaComposer.em.persist(entity);
    }
}
