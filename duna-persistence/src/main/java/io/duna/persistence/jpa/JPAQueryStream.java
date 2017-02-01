/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.persistence.jpa;

import io.duna.persistence.stream.QueryStream;
import org.jinq.jpa.JPAJinqStream;
import org.jinq.orm.stream.JinqStream;
import org.jinq.tuples.*;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface JPAQueryStream<T> extends QueryStream<T>, JPAJinqStream<T> {

    @Override
    <E extends Exception> JPAQueryStream<T> where(Where<T, E> test);

    @Override
    <E extends Exception> JPAQueryStream<T> where(WhereWithSource<T, E> test);

    @Override
    <U> JPAQueryStream<U> select(Select<T, U> select);

    @Override
    <U> JPAQueryStream<U> select(SelectWithSource<T, U> select);

    @Override
    <U> JPAQueryStream<U> selectAll(Join<T, U> select);

    @Override
    <U> JPAQueryStream<U> selectAll(JoinWithSource<T, U> select);

    @Override
    <U> JPAQueryStream<U> selectAllList(JoinToIterable<T, U> select);

    @Override
    <U> JPAQueryStream<Pair<T, U>> join(Join<T, U> join);

    @Override
    <U> JPAQueryStream<Pair<T, U>> join(JoinWithSource<T, U> join);

    @Override
    <U> JPAQueryStream<Pair<T, U>> joinList(JoinToIterable<T, U> join);

    @Override
    <U> JPAQueryStream<Pair<T, U>> leftOuterJoin(Join<T, U> join);

    @Override
    <U> JPAQueryStream<Pair<T, U>> leftOuterJoinList(JoinToIterable<T, U> join);

    @Override
    <U> JPAQueryStream<Pair<T, U>> leftOuterJoin(JoinWithSource<T, U> join,
                                              WhereForOn<T, U> on);

    @Override
    <U> JPAQueryStream<Pair<T, U>> crossJoin(JinqStream<U> join);

    @Override
    <U, V> JPAQueryStream<Pair<U, V>> group(Select<T, U> select,
                                         AggregateGroup<U, T, V> aggregate);

    @Override
    <U, V, W> JPAQueryStream<Tuple3<U, V, W>> group(Select<T, U> select,
                                                 AggregateGroup<U, T, V> aggregate1,
                                                 AggregateGroup<U, T, W> aggregate2);

    @Override
    <U, V, W, X> JPAQueryStream<Tuple4<U, V, W, X>> group(Select<T, U> select,
                                                       AggregateGroup<U, T, V> aggregate1,
                                                       AggregateGroup<U, T, W> aggregate2,
                                                       AggregateGroup<U, T, X> aggregate3);

    @Override
    <U, V, W, X, Y> JPAQueryStream<Tuple5<U, V, W, X, Y>> group(Select<T, U> select,
                                                             AggregateGroup<U, T, V> aggregate1,
                                                             AggregateGroup<U, T, W> aggregate2,
                                                             AggregateGroup<U, T, X> aggregate3,
                                                             AggregateGroup<U, T, Y> aggregate4);

    @Override
    <U, V, W, X, Y, Z> JPAQueryStream<Tuple6<U, V, W, X, Y, Z>> group(Select<T, U> select,
                                                                   AggregateGroup<U, T, V> aggregate1,
                                                                   AggregateGroup<U, T, W> aggregate2,
                                                                   AggregateGroup<U, T, X> aggregate3,
                                                                   AggregateGroup<U, T, Y> aggregate4,
                                                                   AggregateGroup<U, T, Z> aggregate5);

    @Override
    <U, V, W, X, Y, Z, A> JPAQueryStream<Tuple7<U, V, W, X, Y, Z, A>> group(Select<T, U> select,
                                                                         AggregateGroup<U, T, V> aggregate1,
                                                                         AggregateGroup<U, T, W> aggregate2,
                                                                         AggregateGroup<U, T, X> aggregate3,
                                                                         AggregateGroup<U, T, Y> aggregate4,
                                                                         AggregateGroup<U, T, Z> aggregate5,
                                                                         AggregateGroup<U, T, A> aggregate6);

    @Override
    <U, V, W, X, Y, Z, A, B> JPAQueryStream<Tuple8<U, V, W, X, Y, Z, A, B>> group(
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
    <V extends Comparable<V>> JPAQueryStream<T> sortedBy(CollectComparable<T, V> sortField);

    @Override
    <V extends Comparable<V>> JPAQueryStream<T> sortedDescendingBy(
        CollectComparable<T, V> sortField);

    @Override
    JPAQueryStream<T> skip(long n);

    @Override
    JPAQueryStream<T> limit(long n);

    @Override
    JPAQueryStream<T> distinct();

    //

    @Override
    default <U> JPAQueryStream<T> joinFetch(Join<T, U> join) {
        return null;
    }

    @Override
    default <U> JPAQueryStream<T> joinFetchList(JoinToIterable<T, U> join) {
        return null;
    }

    @Override
    default <U> JPAQueryStream<T> leftOuterJoinFetch(Join<T, U> join) {
        return null;
    }

    @Override
    default <U> JPAQueryStream<T> leftOuterJoinFetchList(JoinToIterable<T, U> join) {
        return null;
    }

    @Override
    default JPAQueryStream<T> orUnion(JPAJinqStream<T> otherSet) {
        return null;
    }

    @Override
    default JPAQueryStream<T> andIntersect(JPAJinqStream<T> otherSet) {
        return null;
    }

    @Override
    default JPAQueryStream<T> setHint(String name, Object value) {
        return null;
    }
}
