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
import org.jinq.orm.stream.InQueryStreamSource;
import org.jinq.orm.stream.JinqStream;
import org.jinq.orm.stream.LazyWrappedStream;
import org.jinq.tuples.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class JPAQueryStreamWrapper<T> extends LazyWrappedStream<T> implements JPAQueryStream<T> {

    JinqStream<T> wrapped;

    public JPAQueryStreamWrapper(JinqStream<T> wrapped) {
        super(wrapped);
        this.wrapped = wrapped;
    }

    private <U> JPAQueryStream<U> wrap(JinqStream<U> toWrap) {
        return new JPAQueryStreamWrapper<U>(toWrap);
    }

    @Override
    public Long sumInteger(
        org.jinq.orm.stream.JinqStream.CollectInteger<T> aggregate) {
        return wrapped.sumInteger(aggregate);
    }

    @Override
    public Long sumLong(org.jinq.orm.stream.JinqStream.CollectLong<T> aggregate) {
        return wrapped.sumLong(aggregate);
    }

    @Override
    public Double sumDouble(
        org.jinq.orm.stream.JinqStream.CollectDouble<T> aggregate) {
        return wrapped.sumDouble(aggregate);
    }

    @Override
    public BigDecimal sumBigDecimal(
        org.jinq.orm.stream.JinqStream.CollectBigDecimal<T> aggregate) {
        return wrapped.sumBigDecimal(aggregate);
    }

    @Override
    public BigInteger sumBigInteger(
        org.jinq.orm.stream.JinqStream.CollectBigInteger<T> aggregate) {
        return wrapped.sumBigInteger(aggregate);
    }

    @Override
    public <V extends Comparable<V>> V max(
        org.jinq.orm.stream.JinqStream.CollectComparable<T, V> aggregate) {
        return wrapped.max(aggregate);
    }

    @Override
    public <V extends Comparable<V>> V min(
        org.jinq.orm.stream.JinqStream.CollectComparable<T, V> aggregate) {
        return wrapped.min(aggregate);
    }

    @Override
    public <V extends Number & Comparable<V>> Double avg(
        org.jinq.orm.stream.JinqStream.CollectNumber<T, V> aggregate) {
        return wrapped.avg(aggregate);
    }

    @Override
    public <U, V> Pair<U, V> aggregate(
        org.jinq.orm.stream.JinqStream.AggregateSelect<T, U> aggregate1,
        org.jinq.orm.stream.JinqStream.AggregateSelect<T, V> aggregate2) {
        return wrapped.aggregate(aggregate1, aggregate2);
    }

    @Override
    public <U, V, W> Tuple3<U, V, W> aggregate(
        org.jinq.orm.stream.JinqStream.AggregateSelect<T, U> aggregate1,
        org.jinq.orm.stream.JinqStream.AggregateSelect<T, V> aggregate2,
        org.jinq.orm.stream.JinqStream.AggregateSelect<T, W> aggregate3) {
        return wrapped.aggregate(aggregate1, aggregate2, aggregate3);
    }

    @Override
    public <U, V, W, X> Tuple4<U, V, W, X> aggregate(
        org.jinq.orm.stream.JinqStream.AggregateSelect<T, U> aggregate1,
        org.jinq.orm.stream.JinqStream.AggregateSelect<T, V> aggregate2,
        org.jinq.orm.stream.JinqStream.AggregateSelect<T, W> aggregate3,
        org.jinq.orm.stream.JinqStream.AggregateSelect<T, X> aggregate4) {
        return wrapped.aggregate(aggregate1, aggregate2, aggregate3, aggregate4);
    }

    @Override
    public <U, V, W, X, Y> Tuple5<U, V, W, X, Y> aggregate(
        org.jinq.orm.stream.JinqStream.AggregateSelect<T, U> aggregate1,
        org.jinq.orm.stream.JinqStream.AggregateSelect<T, V> aggregate2,
        org.jinq.orm.stream.JinqStream.AggregateSelect<T, W> aggregate3,
        org.jinq.orm.stream.JinqStream.AggregateSelect<T, X> aggregate4,
        org.jinq.orm.stream.JinqStream.AggregateSelect<T, Y> aggregate5) {
        return wrapped.aggregate(aggregate1, aggregate2, aggregate3, aggregate4, aggregate5);
    }

    @Override
    public long count() {
        return wrapped.count();
    }

    @Override
    public Optional<T> findOne() {
        return wrapped.findOne();
    }

    @Override
    public T getOnlyValue() {
        return wrapped.getOnlyValue();
    }

    @Override
    public List<T> toList() {
        return wrapped.toList();
    }

    @Override
    public String getDebugQueryString() {
        return wrapped.getDebugQueryString();
    }

    @Override
    public void propagateException(Object source, Throwable exception) {
        wrapped.propagateException(source, exception);
    }

    @Override
    public Collection<Throwable> getExceptions() {
        return wrapped.getExceptions();
    }

    @Override
    public <E extends Exception> JPAQueryStream<T> where(
        org.jinq.orm.stream.JinqStream.Where<T, E> test) {
        return wrap(wrapped.where(test));
    }

    @Override
    public <E extends Exception> JPAQueryStream<T> where(
        org.jinq.orm.stream.JinqStream.WhereWithSource<T, E> test) {
        return wrap(wrapped.where(test));
    }

    @Override
    public <U> JPAQueryStream<U> select(
        org.jinq.orm.stream.JinqStream.Select<T, U> select) {
        return wrap(wrapped.select(select));
    }

    @Override
    public <U> JPAQueryStream<U> select(
        org.jinq.orm.stream.JinqStream.SelectWithSource<T, U> select) {
        return wrap(wrapped.select(select));
    }

    @Override
    public <U> JPAQueryStream<U> selectAll(
        org.jinq.orm.stream.JinqStream.Join<T, U> select) {
        return wrap(wrapped.selectAll(select));
    }

    @Override
    public <U> JPAQueryStream<U> selectAll(
        org.jinq.orm.stream.JinqStream.JoinWithSource<T, U> select) {
        return wrap(wrapped.selectAll(select));
    }

    @Override
    public <U> JPAQueryStream<U> selectAllList(
        org.jinq.orm.stream.JinqStream.JoinToIterable<T, U> select) {
        return wrap(wrapped.selectAllList(select));
    }

    @Override
    public <U> JPAQueryStream<Pair<T, U>> join(
        org.jinq.orm.stream.JinqStream.Join<T, U> join) {
        return wrap(wrapped.join(join));
    }

    @Override
    public <U> JPAQueryStream<Pair<T, U>> join(
        org.jinq.orm.stream.JinqStream.JoinWithSource<T, U> join) {
        return wrap(wrapped.join(join));
    }

    @Override
    public <U> JPAQueryStream<Pair<T, U>> joinList(
        org.jinq.orm.stream.JinqStream.JoinToIterable<T, U> join) {
        return wrap(wrapped.joinList(join));
    }

    @Override
    public <U> JPAQueryStream<Pair<T, U>> leftOuterJoin(
        org.jinq.orm.stream.JinqStream.Join<T, U> join) {
        return wrap(wrapped.leftOuterJoin(join));
    }

    @Override
    public <U> JPAQueryStream<Pair<T, U>> leftOuterJoinList(
        org.jinq.orm.stream.JinqStream.JoinToIterable<T, U> join) {
        return wrap(wrapped.leftOuterJoinList(join));
    }

    @Override
    public <U> JPAQueryStream<Pair<T, U>> leftOuterJoin(
        org.jinq.orm.stream.JinqStream.JoinWithSource<T, U> join,
        org.jinq.orm.stream.JinqStream.WhereForOn<T, U> on) {
        return wrap(wrapped.leftOuterJoin(join, on));
    }

    @Override
    public <U> JPAQueryStream<Pair<T, U>> crossJoin(JinqStream<U> join) {
        return wrap(wrapped.crossJoin(join));
    }


    @Override
    public <U, V> JPAQueryStream<Pair<U, V>> group(
        org.jinq.orm.stream.JinqStream.Select<T, U> select,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, V> aggregate) {
        return wrap(wrapped.group(select, aggregate));
    }

    @Override
    public <U, V, W> JPAQueryStream<Tuple3<U, V, W>> group(
        org.jinq.orm.stream.JinqStream.Select<T, U> select,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, V> aggregate1,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, W> aggregate2) {
        return wrap(wrapped.group(select, aggregate1, aggregate2));
    }

    @Override
    public <U, V, W, X> JPAQueryStream<Tuple4<U, V, W, X>> group(
        org.jinq.orm.stream.JinqStream.Select<T, U> select,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, V> aggregate1,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, W> aggregate2,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, X> aggregate3) {
        return wrap(wrapped.group(select, aggregate1, aggregate2, aggregate3));
    }

    @Override
    public <U, V, W, X, Y> JPAQueryStream<Tuple5<U, V, W, X, Y>> group(
        org.jinq.orm.stream.JinqStream.Select<T, U> select,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, V> aggregate1,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, W> aggregate2,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, X> aggregate3,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, Y> aggregate4) {
        return wrap(wrapped.group(select, aggregate1, aggregate2, aggregate3, aggregate4));
    }

    @Override
    public <U, V, W, X, Y, Z> JPAQueryStream<Tuple6<U, V, W, X, Y, Z>> group(
        org.jinq.orm.stream.JinqStream.Select<T, U> select,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, V> aggregate1,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, W> aggregate2,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, X> aggregate3,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, Y> aggregate4,
        org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, Z> aggregate5) {
        return wrap(wrapped.group(select, aggregate1, aggregate2, aggregate3,
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
        return wrap(wrapped.group(select, aggregate1, aggregate2, aggregate3,
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
        return wrap(wrapped.group(select, aggregate1, aggregate2, aggregate3,
                                  aggregate4, aggregate5, aggregate6, aggregate7));
    }

    @Override
    public <V extends Comparable<V>> JPAQueryStream<T> sortedBy(
        org.jinq.orm.stream.JinqStream.CollectComparable<T, V> sortField) {
        return wrap(wrapped.sortedBy(sortField));
    }

    @Override
    public <V extends Comparable<V>> JPAQueryStream<T> sortedDescendingBy(
        org.jinq.orm.stream.JinqStream.CollectComparable<T, V> sortField) {
        return wrap(wrapped.sortedDescendingBy(sortField));
    }

    @Override
    public JPAQueryStream<T> skip(long n) {
        return wrap(wrapped.skip(n));
    }

    @Override
    public JPAQueryStream<T> limit(long n) {
        return wrap(wrapped.limit(n));
    }

    @Override
    public JPAQueryStream<T> distinct() {
        return wrap(wrapped.distinct());
    }

    @Override
    public JPAQueryStream<T> setHint(String name, Object value) {
        return wrap(wrapped.setHint(name, value));
    }


    // New JPAQueryStream API

    // TODO: These methods don't properly emulate the full behavior of
    //   JOIN FETCH and LEFT OUTER JOIN FETCH

    @Override
    public <U> JPAQueryStream<T> joinFetch(
        org.jinq.orm.stream.JinqStream.Join<T, U> join) {
        return this;
    }

    @Override
    public <U> JPAQueryStream<T> joinFetchList(
        org.jinq.orm.stream.JinqStream.JoinToIterable<T, U> join) {
        return this;
    }

    @Override
    public <U> JPAQueryStream<T> leftOuterJoinFetch(
        org.jinq.orm.stream.JinqStream.Join<T, U> join) {
        return this;
    }

    @Override
    public <U> JPAQueryStream<T> leftOuterJoinFetchList(
        org.jinq.orm.stream.JinqStream.JoinToIterable<T, U> join) {
        return this;
    }

    @Override
    public JPAQueryStream<T> orUnion(JPAJinqStream<T> otherSet) {
        Set<T> merged = collect(Collectors.toSet());
        merged.addAll(otherSet.collect(Collectors.toSet()));
        return wrap(JinqStream.from(merged));
    }

    @Override
    public JPAQueryStream<T> andIntersect(JPAJinqStream<T> otherSet) {
        Set<T> saved = collect(Collectors.toSet());
        return wrap(JinqStream.from(otherSet.filter(saved::contains).collect(Collectors.toSet())));
    }

    @Override
    public void persist(Object entity) {
        if (wrapped instanceof JPAQueryStream) {
            ((JPAQueryStream<T>) wrapped).persist(entity);
        }
    }

    @Override
    public <V> V merge(V entity) {
        if (wrapped instanceof JPAQueryStream) {
            return ((JPAQueryStream<T>) wrapped).merge(entity);
        }

        return entity;
    }

    @Override
    public void remove(Object entity) {
        if (wrapped instanceof JPAQueryStream) {
            ((JPAQueryStream<T>) wrapped).remove(entity);
        }
    }
}
