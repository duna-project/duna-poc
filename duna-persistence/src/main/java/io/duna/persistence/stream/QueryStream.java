/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.persistence.stream;

import org.jinq.orm.stream.InQueryStreamSource;
import org.jinq.orm.stream.JinqStream;
import org.jinq.tuples.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface QueryStream<T> extends JinqStream<T> {

    <V> V merge(V entity);

    default void merge() {
        this.forEach(this::merge);
    }

    default void flush() {}

    default void begin() {}

    default void commit() {}

    default void rollback() {}

    void remove(Object entity);

    void persist(Object entity);

    @Override
    <E extends Exception> QueryStream<T> where(Where<T, E> test);

    @Override
    <E extends Exception> QueryStream<T> where(WhereWithSource<T, E> test);

    @Override
    <U> QueryStream<U> select(Select<T, U> select);

    @Override
    <U> QueryStream<U> select(SelectWithSource<T, U> select);

    @Override
    <U> QueryStream<U> selectAll(Join<T, U> select);

    @Override
    <U> QueryStream<U> selectAll(JoinWithSource<T, U> select);

    @Override
    <U> QueryStream<U> selectAllList(JoinToIterable<T, U> select);

    @Override
    <U> QueryStream<Pair<T, U>> join(Join<T, U> join);

    @Override
    <U> QueryStream<Pair<T, U>> join(JoinWithSource<T, U> join);

    @Override
    <U> QueryStream<Pair<T, U>> joinList(JoinToIterable<T, U> join);

    @Override
    <U> QueryStream<Pair<T, U>> leftOuterJoin(Join<T, U> join);

    @Override
    <U> QueryStream<Pair<T, U>> leftOuterJoinList(JoinToIterable<T, U> join);

    @Override
    <U> QueryStream<Pair<T, U>> leftOuterJoin(JoinWithSource<T, U> join,
                                             WhereForOn<T, U> on);

    @Override
    <U> QueryStream<Pair<T, U>> crossJoin(JinqStream<U> join);

    @Override
    <U, V> QueryStream<Pair<U, V>> group(Select<T, U> select,
                                        AggregateGroup<U, T, V> aggregate);

    @Override
    <U, V, W> QueryStream<Tuple3<U, V, W>> group(Select<T, U> select,
                                                AggregateGroup<U, T, V> aggregate1,
                                                AggregateGroup<U, T, W> aggregate2);

    @Override
    <U, V, W, X> QueryStream<Tuple4<U, V, W, X>> group(Select<T, U> select,
                                                      AggregateGroup<U, T, V> aggregate1,
                                                      AggregateGroup<U, T, W> aggregate2,
                                                      AggregateGroup<U, T, X> aggregate3);

    @Override
    <U, V, W, X, Y> QueryStream<Tuple5<U, V, W, X, Y>> group(Select<T, U> select,
                                                            AggregateGroup<U, T, V> aggregate1,
                                                            AggregateGroup<U, T, W> aggregate2,
                                                            AggregateGroup<U, T, X> aggregate3,
                                                            AggregateGroup<U, T, Y> aggregate4);

    @Override
    <U, V, W, X, Y, Z> QueryStream<Tuple6<U, V, W, X, Y, Z>> group(Select<T, U> select,
                                                                  AggregateGroup<U, T, V> aggregate1,
                                                                  AggregateGroup<U, T, W> aggregate2,
                                                                  AggregateGroup<U, T, X> aggregate3,
                                                                  AggregateGroup<U, T, Y> aggregate4,
                                                                  AggregateGroup<U, T, Z> aggregate5);

    @Override
    <U, V, W, X, Y, Z, A> QueryStream<Tuple7<U, V, W, X, Y, Z, A>> group(Select<T, U> select,
                                                                        AggregateGroup<U, T, V> aggregate1,
                                                                        AggregateGroup<U, T, W> aggregate2,
                                                                        AggregateGroup<U, T, X> aggregate3,
                                                                        AggregateGroup<U, T, Y> aggregate4,
                                                                        AggregateGroup<U, T, Z> aggregate5,
                                                                        AggregateGroup<U, T, A> aggregate6);

    @Override
    <U, V, W, X, Y, Z, A, B> QueryStream<Tuple8<U, V, W, X, Y, Z, A, B>> group(
        Select<T, U> select, AggregateGroup<U, T, V> aggregate1,
        AggregateGroup<U, T, W> aggregate2, AggregateGroup<U, T, X> aggregate3,
        AggregateGroup<U, T, Y> aggregate4, AggregateGroup<U, T, Z> aggregate5,
        AggregateGroup<U, T, A> aggregate6, AggregateGroup<U, T, B> aggregate7);

    @Override
    Long sumInteger(CollectInteger<T> aggregate);

    @Override
    Long sumLong(CollectLong<T> aggregate);

    @Override
    Double sumDouble(CollectDouble<T> aggregate);

    @Override
    BigDecimal sumBigDecimal(CollectBigDecimal<T> aggregate);

    @Override
    BigInteger sumBigInteger(CollectBigInteger<T> aggregate);

    @Override
    <V extends Comparable<V>> V max(CollectComparable<T, V> aggregate);

    @Override
    <V extends Comparable<V>> V min(CollectComparable<T, V> aggregate);

    @Override
    <V extends Number & Comparable<V>> Double avg(CollectNumber<T, V> aggregate);

    @Override
    <U, V> Pair<U, V> aggregate(AggregateSelect<T, U> aggregate1,
                                AggregateSelect<T, V> aggregate2);

    @Override
    <U, V, W> Tuple3<U, V, W> aggregate(AggregateSelect<T, U> aggregate1,
                                        AggregateSelect<T, V> aggregate2,
                                        AggregateSelect<T, W> aggregate3);

    @Override
    <U, V, W, X> Tuple4<U, V, W, X> aggregate(AggregateSelect<T, U> aggregate1,
                                              AggregateSelect<T, V> aggregate2,
                                              AggregateSelect<T, W> aggregate3,
                                              AggregateSelect<T, X> aggregate4);

    @Override
    <U, V, W, X, Y> Tuple5<U, V, W, X, Y> aggregate(AggregateSelect<T, U> aggregate1,
                                                    AggregateSelect<T, V> aggregate2,
                                                    AggregateSelect<T, W> aggregate3,
                                                    AggregateSelect<T, X> aggregate4,
                                                    AggregateSelect<T, Y> aggregate5);

    @Override
    <V extends Comparable<V>> QueryStream<T> sortedBy(CollectComparable<T, V> sortField);

    @Override
    <V extends Comparable<V>> QueryStream<T> sortedDescendingBy(
        CollectComparable<T, V> sortField);

    @Override
    QueryStream<T> skip(long n);

    @Override
    QueryStream<T> limit(long n);

    @Override
    QueryStream<T> distinct();

    @Override
    QueryStream<T> setHint(String name, Object value);

    default void forEach(BiConsumer<? super T, QueryStream<T>> action) {
        this.forEach(t -> action.accept(t, this));
    }
}
