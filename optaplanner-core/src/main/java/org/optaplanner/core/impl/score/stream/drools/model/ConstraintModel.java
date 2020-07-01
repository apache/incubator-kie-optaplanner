/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.score.stream.drools.model;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongBiFunction;
import java.util.function.ToLongFunction;

import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.function.QuadPredicate;
import org.optaplanner.core.api.function.ToIntQuadFunction;
import org.optaplanner.core.api.function.ToIntTriFunction;
import org.optaplanner.core.api.function.ToLongQuadFunction;
import org.optaplanner.core.api.function.ToLongTriFunction;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.function.TriPredicate;
import org.optaplanner.core.api.score.stream.bi.BiConstraintCollector;
import org.optaplanner.core.api.score.stream.quad.QuadConstraintCollector;
import org.optaplanner.core.api.score.stream.tri.TriConstraintCollector;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;
import org.optaplanner.core.api.score.stream.uni.UniConstraintStream;
import org.optaplanner.core.impl.score.stream.bi.AbstractBiJoiner;
import org.optaplanner.core.impl.score.stream.penta.AbstractPentaJoiner;
import org.optaplanner.core.impl.score.stream.quad.AbstractQuadJoiner;
import org.optaplanner.core.impl.score.stream.tri.AbstractTriJoiner;

public final class ConstraintModel {

    private final Map<Class, FromNode> sourceNodeMap = new LinkedHashMap<>(0);

    public <A> UniConstraintModelNode<A> getSourceNode(Class<A> clz) {
        return sourceNodeMap.computeIfAbsent(clz, FromNode::new);
    }

    public <A> UniConstraintModelNode<A> filter(UniConstraintModelNode<A> parent, Predicate<A> predicate) {
        throw new UnsupportedOperationException();
    }

    public <A, B> BiConstraintModelNode<A, B> filter(BiConstraintModelNode<A, B> parent, BiPredicate<A, B> predicate) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C> TriConstraintModelNode<A, B, C> filter(TriConstraintModelNode<A, B, C> parent,
            TriPredicate<A, B, C> predicate) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C, D> QuadConstraintModelNode<A, B, C, D> filter(QuadConstraintModelNode<A, B, C, D> parent,
            QuadPredicate<A, B, C, D> predicate) {
        throw new UnsupportedOperationException();
    }

    public <A, B> BiConstraintModelNode<A, B> join(UniConstraintModelNode<A> leftParent,
            UniConstraintModelNode<A> rightParent, AbstractBiJoiner<A, B> joiner) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C> TriConstraintModelNode<A, B, C> join(BiConstraintModelNode<A, B> leftParent,
            UniConstraintModelNode<C> rightParent, AbstractTriJoiner<A, B, C> joiner) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C, D> QuadConstraintModelNode<A, B, C, D> join(TriConstraintModelNode<A, B, C> leftParent,
            UniConstraintModelNode<D> rightParent, AbstractQuadJoiner<A, B, C, D> joiner) {
        throw new UnsupportedOperationException();
    }

    public <A, B> UniConstraintModelNode<A> ifExists(UniConstraintModelNode<A> leftParent,
            UniConstraintModelNode<A> rightParent, AbstractBiJoiner<A, B> joiner) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C> BiConstraintModelNode<A, B> ifExists(BiConstraintModelNode<A, B> leftParent,
            UniConstraintModelNode<C> rightParent, AbstractTriJoiner<A, B, C> joiner) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C, D> TriConstraintModelNode<A, B, C> ifExists(TriConstraintModelNode<A, B, C> leftParent,
            UniConstraintModelNode<D> rightParent, AbstractQuadJoiner<A, B, C, D> joiner) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C, D, E> QuadConstraintModelNode<A, B, C, D> ifExists(QuadConstraintModelNode<A, B, C, D> leftParent,
            UniConstraintModelNode<E> rightParent, AbstractPentaJoiner<A, B, C, D, E> joiner) {
        throw new UnsupportedOperationException();
    }

    public <A, B> UniConstraintModelNode<A> ifNotExists(UniConstraintModelNode<A> leftParent,
            UniConstraintModelNode<A> rightParent, AbstractBiJoiner<A, B> joiner) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C> BiConstraintModelNode<A, B> ifNotExists(BiConstraintModelNode<A, B> leftParent,
            UniConstraintModelNode<C> rightParent, AbstractTriJoiner<A, B, C> joiner) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C, D> TriConstraintModelNode<A, B, C> ifNotExists(TriConstraintModelNode<A, B, C> leftParent,
            UniConstraintModelNode<D> rightParent, AbstractQuadJoiner<A, B, C, D> joiner) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C, D, E> QuadConstraintModelNode<A, B, C, D> ifNotExists(QuadConstraintModelNode<A, B, C, D> leftParent,
            UniConstraintModelNode<E> rightParent, AbstractPentaJoiner<A, B, C, D, E> joiner) {
        throw new UnsupportedOperationException();
    }

    public <A, GroupKey_> UniConstraintModelNode<GroupKey_> groupBy(UniConstraintModelNode<A> parent,
            Function<A, GroupKey_> mapping) {
        throw new UnsupportedOperationException();
    }

    public <A, GroupKeyA_, GroupKeyB_> BiConstraintModelNode<GroupKeyA_, GroupKeyB_> groupBy(
            UniConstraintModelNode<A> parent, Function<A, GroupKeyA_> aMapping, Function<A, GroupKeyB_> bMapping) {
        throw new UnsupportedOperationException();
    }

    public <A, ResultContainer_, Result_> UniConstraintStream<Result_> groupBy(UniConstraintModelNode<A> parent,
            UniConstraintCollector<A, ResultContainer_, Result_> collector) {
        throw new UnsupportedOperationException();
    }

    public <A, GroupKey_, ResultContainer_, Result_> BiConstraintModelNode<GroupKey_, Result_> groupBy(
            UniConstraintModelNode<A> parent, Function<A, GroupKey_> mapping,
            UniConstraintCollector<A, ResultContainer_, Result_> collector) {
        throw new UnsupportedOperationException();
    }

    public <A, GroupKeyA_, GroupKeyB_, ResultContainer_, Result_>
            TriConstraintModelNode<GroupKeyA_, GroupKeyB_, Result_> groupBy(UniConstraintModelNode<A> parent,
                    Function<A, GroupKeyA_> aMapping, Function<A, GroupKeyA_> bMapping,
                    UniConstraintCollector<A, ResultContainer_, Result_> collector) {
        throw new UnsupportedOperationException();
    }

    public <A, GroupKeyA_, GroupKeyB_, ResultContainerC_, ResultC_, ResultContainerD_, ResultD_>
            QuadConstraintModelNode<GroupKeyA_, GroupKeyB_, ResultC_, ResultD_> groupBy(UniConstraintModelNode<A> parent,
                    Function<A, GroupKeyA_> aMapping, Function<A, GroupKeyB_> bMapping,
                    UniConstraintCollector<A, ResultContainerC_, ResultC_> cCollector,
                    UniConstraintCollector<A, ResultContainerD_, ResultD_> dCollector) {
        throw new UnsupportedOperationException();
    }

    public <A, B, ResultContainer_, Result_> UniConstraintModelNode<Result_> groupBy(BiConstraintModelNode<A, B> parent,
            BiConstraintCollector<A, B, ResultContainer_, Result_> collector) {
        throw new UnsupportedOperationException();
    }

    public <A, B, GroupKey_> UniConstraintModelNode<GroupKey_> groupBy(BiConstraintModelNode<A, B> parent,
            BiFunction<A, B, GroupKey_> mapping) {
        throw new UnsupportedOperationException();
    }

    public <A, B, GroupKey_, ResultContainer_, Result_> BiConstraintModelNode<GroupKey_, Result_> groupBy(
            BiConstraintModelNode<A, B> parent, BiFunction<A, B, GroupKey_> mapping,
            BiConstraintCollector<A, B, ResultContainer_, Result_> collector) {
        throw new UnsupportedOperationException();
    }

    public <A, B, GroupKeyA_, GroupKeyB_> BiConstraintModelNode<GroupKeyA_, GroupKeyB_> groupBy(
            BiConstraintModelNode<A, B> parent, BiFunction<A, B, GroupKeyA_> aMapping,
            BiFunction<A, B, GroupKeyB_> bMapping) {
        throw new UnsupportedOperationException();
    }

    public <A, B, GroupKeyA_, GroupKeyB_, ResultContainer_, Result_>
            TriConstraintModelNode<GroupKeyA_, GroupKeyB_, Result_> groupBy(BiConstraintModelNode<A, B> parent,
                    BiFunction<A, B, GroupKeyA_> aMapping, BiFunction<A, B, GroupKeyB_> bMapping,
                    BiConstraintCollector<A, B, ResultContainer_, Result_> collector) {
        throw new UnsupportedOperationException();
    }

    public <A, B, GroupKeyA_, GroupKeyB_, ResultContainerC_, ResultC_, ResultContainerD_, ResultD_>
            QuadConstraintModelNode<GroupKeyA_, GroupKeyB_, ResultC_, ResultD_> groupBy(BiConstraintModelNode<A, B> parent,
                    BiFunction<A, B, GroupKeyA_> aMapping, BiFunction<A, B, GroupKeyB_> bMapping,
                    BiConstraintCollector<A, B, ResultContainerC_, ResultC_> cCollector,
                    BiConstraintCollector<A, B, ResultContainerD_, ResultD_> dCollector) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C, ResultContainer_, Result_> UniConstraintModelNode<Result_> groupBy(
            TriConstraintModelNode<A, B, C> parent,
            TriConstraintCollector<A, B, C, ResultContainer_, Result_> collector) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C, GroupKey_> UniConstraintModelNode<GroupKey_> groupBy(TriConstraintModelNode<A, B, C> parent,
            TriFunction<A, B, C, GroupKey_> mapping) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C, GroupKey_, ResultContainer_, Result_> BiConstraintModelNode<GroupKey_, Result_> groupBy(
            TriConstraintModelNode<A, B, C> parent, TriFunction<A, B, C, GroupKey_> mapping,
            TriConstraintCollector<A, B, C, ResultContainer_, Result_> collector) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C, GroupKeyA_, GroupKeyB_> BiConstraintModelNode<GroupKeyA_, GroupKeyB_> groupBy(
            TriConstraintModelNode<A, B, C> parent, TriFunction<A, B, C, GroupKeyA_> aMapping,
            TriFunction<A, B, C, GroupKeyB_> bMapping) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C, GroupKeyA_, GroupKeyB_, ResultContainer_, Result_>
            TriConstraintModelNode<GroupKeyA_, GroupKeyB_, Result_> groupBy(TriConstraintModelNode<A, B, C> parent,
                    TriFunction<A, B, C, GroupKeyA_> aMapping, TriFunction<A, B, C, GroupKeyB_> bMapping,
                    TriConstraintCollector<A, B, C, ResultContainer_, Result_> collector) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C, GroupKeyA_, GroupKeyB_, ResultContainerC_, ResultC_, ResultContainerD_, ResultD_>
            QuadConstraintModelNode<GroupKeyA_, GroupKeyB_, ResultC_, ResultD_> groupBy(TriConstraintModelNode<A, B, C> parent,
                    TriFunction<A, B, C, GroupKeyA_> aMapping, TriFunction<A, B, C, GroupKeyB_> bMapping,
                    TriConstraintCollector<A, B, C, ResultContainerC_, ResultC_> cCollector,
                    TriConstraintCollector<A, B, C, ResultContainerD_, ResultD_> dCollector) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C, D, ResultContainer_, Result_> UniConstraintModelNode<Result_> groupBy(
            QuadConstraintModelNode<A, B, C, D> parent,
            QuadConstraintCollector<A, B, C, D, ResultContainer_, Result_> collector) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C, D, GroupKey_> UniConstraintModelNode<GroupKey_> groupBy(QuadConstraintModelNode<A, B, C, D> parent,
            QuadFunction<A, B, C, D, GroupKey_> mapping) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C, D, GroupKey_, ResultContainer_, Result_> BiConstraintModelNode<GroupKey_, Result_> groupBy(
            QuadConstraintModelNode<A, B, C, D> parent, QuadFunction<A, B, C, D, GroupKey_> mapping,
            QuadConstraintCollector<A, B, C, D, ResultContainer_, Result_> collector) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C, D, GroupKeyA_, GroupKeyB_> BiConstraintModelNode<GroupKeyA_, GroupKeyB_> groupBy(
            QuadConstraintModelNode<A, B, C, D> parent, QuadFunction<A, B, C, D, GroupKeyA_> aMapping,
            QuadFunction<A, B, C, D, GroupKeyB_> bMapping) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C, D, GroupKeyA_, GroupKeyB_, ResultContainer_, Result_>
            TriConstraintModelNode<GroupKeyA_, GroupKeyB_, Result_> groupBy(QuadConstraintModelNode<A, B, C, D> parent,
                    QuadFunction<A, B, C, D, GroupKeyA_> aMapping, QuadFunction<A, B, C, D, GroupKeyB_> bMapping,
                    QuadConstraintCollector<A, B, C, D, ResultContainer_, Result_> collector) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C, D, GroupKeyA_, GroupKeyB_, ResultContainerC_, ResultC_, ResultContainerD_, ResultD_>
            QuadConstraintModelNode<GroupKeyA_, GroupKeyB_, ResultC_, ResultD_> groupBy(
                    QuadConstraintModelNode<A, B, C, D> parent, QuadFunction<A, B, C, D, GroupKeyA_> aMapping,
                    QuadFunction<A, B, C, D, GroupKeyB_> bMapping,
                    QuadConstraintCollector<A, B, C, D, ResultContainerC_, ResultC_> cCollector,
                    QuadConstraintCollector<A, B, C, D, ResultContainerD_, ResultD_> dCollector) {
        throw new UnsupportedOperationException();
    }

    public <A> UniConstraintModelNode<A> impact(UniConstraintModelNode<A> parent) {
        throw new UnsupportedOperationException();
    }

    public <A> UniConstraintModelNode<A> impact(UniConstraintModelNode<A> parent, ToIntFunction<A> matchWeighter) {
        throw new UnsupportedOperationException();
    }

    public <A> UniConstraintModelNode<A> impact(UniConstraintModelNode<A> parent, ToLongFunction<A> matchWeighter) {
        throw new UnsupportedOperationException();
    }

    public <A> UniConstraintModelNode<A> impact(UniConstraintModelNode<A> parent,
            Function<A, BigDecimal> matchWeighter) {
        throw new UnsupportedOperationException();
    }

    public <A, B> BiConstraintModelNode<A, B> impact(BiConstraintModelNode<A, B> parent) {
        throw new UnsupportedOperationException();
    }

    public <A, B> BiConstraintModelNode<A, B> impact(BiConstraintModelNode<A, B> parent,
            ToIntBiFunction<A, B> matchWeighter) {
        throw new UnsupportedOperationException();
    }

    public <A, B> BiConstraintModelNode<A, B> impact(BiConstraintModelNode<A, B> parent,
            ToLongBiFunction<A, B> matchWeighter) {
        throw new UnsupportedOperationException();
    }

    public <A, B> BiConstraintModelNode<A, B> impact(BiConstraintModelNode<A, B> parent,
            BiFunction<A, B, BigDecimal> matchWeighter) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C> TriConstraintModelNode<A, B, C> impact(TriConstraintModelNode<A, B, C> parent) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C> TriConstraintModelNode<A, B, C> impact(TriConstraintModelNode<A, B, C> parent,
            ToIntTriFunction<A, B, C> matchWeighter) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C> TriConstraintModelNode<A, B, C> impact(TriConstraintModelNode<A, B, C> parent,
            ToLongTriFunction<A, B, C> matchWeighter) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C> TriConstraintModelNode<A, B, C> impact(TriConstraintModelNode<A, B, C> parent,
            TriFunction<A, B, C, BigDecimal> matchWeighter) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C, D> QuadConstraintModelNode<A, B, C, D> impact(QuadConstraintModelNode<A, B, C, D> parent) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C, D> QuadConstraintModelNode<A, B, C, D> impact(QuadConstraintModelNode<A, B, C, D> parent,
            ToIntQuadFunction<A, B, C, D> matchWeighter) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C, D> QuadConstraintModelNode<A, B, C, D> impact(QuadConstraintModelNode<A, B, C, D> parent,
            ToLongQuadFunction<A, B, C, D> matchWeighter) {
        throw new UnsupportedOperationException();
    }

    public <A, B, C, D> QuadConstraintModelNode<A, B, C, D> impact(QuadConstraintModelNode<A, B, C, D> parent,
            QuadFunction<A, B, C, D, BigDecimal> matchWeighter) {
        throw new UnsupportedOperationException();
    }

}
