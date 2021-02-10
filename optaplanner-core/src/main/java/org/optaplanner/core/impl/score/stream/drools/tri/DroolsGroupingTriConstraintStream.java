/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.score.stream.drools.tri;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.stream.bi.BiConstraintCollector;
import org.optaplanner.core.api.score.stream.quad.QuadConstraintCollector;
import org.optaplanner.core.api.score.stream.tri.TriConstraintCollector;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraintFactory;
import org.optaplanner.core.impl.score.stream.drools.bi.DroolsAbstractBiConstraintStream;
import org.optaplanner.core.impl.score.stream.drools.common.TriLeftHandSide;
import org.optaplanner.core.impl.score.stream.drools.quad.DroolsAbstractQuadConstraintStream;
import org.optaplanner.core.impl.score.stream.drools.uni.DroolsAbstractUniConstraintStream;

public final class DroolsGroupingTriConstraintStream<Solution_, NewA, NewB, NewC>
        extends DroolsAbstractTriConstraintStream<Solution_, NewA, NewB, NewC> {

    private final TriLeftHandSide<NewA, NewB, NewC> leftHandSide;

    public <A, __, ___, ____> DroolsGroupingTriConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractUniConstraintStream<Solution_, A> parent, UniConstraintCollector<A, __, NewA> collectorA,
            UniConstraintCollector<A, ___, NewB> collectorB, UniConstraintCollector<A, ____, NewC> collectorC) {
        super(constraintFactory);
        throw new UnsupportedOperationException();
    }

    public <A, B, __, ___, ____> DroolsGroupingTriConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractBiConstraintStream<Solution_, A, B> parent, BiConstraintCollector<A, B, __, NewA> collectorA,
            BiConstraintCollector<A, B, ___, NewB> collectorB, BiConstraintCollector<A, B, ____, NewC> collectorC) {
        super(constraintFactory);
        throw new UnsupportedOperationException();
    }

    public <A, B, C, __, ___, ____> DroolsGroupingTriConstraintStream(
            DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractTriConstraintStream<Solution_, A, B, C> parent,
            TriConstraintCollector<A, B, C, __, NewA> collectorA,
            TriConstraintCollector<A, B, C, ___, NewB> collectorB,
            TriConstraintCollector<A, B, C, ____, NewC> collectorC) {
        super(constraintFactory);
        throw new UnsupportedOperationException();
    }

    public <A, B, C, D, __, ___, ____> DroolsGroupingTriConstraintStream(
            DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractQuadConstraintStream<Solution_, A, B, C, D> parent,
            QuadConstraintCollector<A, B, C, D, __, NewA> collectorA,
            QuadConstraintCollector<A, B, C, D, ___, NewB> collectorB,
            QuadConstraintCollector<A, B, C, D, ____, NewC> collectorC) {
        super(constraintFactory);
        throw new UnsupportedOperationException();
    }

    public <A, __, ___> DroolsGroupingTriConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractUniConstraintStream<Solution_, A> parent, Function<A, NewA> groupKeyAMapping,
            UniConstraintCollector<A, __, NewB> collectorB, UniConstraintCollector<A, ___, NewC> collectorC) {
        super(constraintFactory);
        throw new UnsupportedOperationException();
    }

    public <A, B, __, ___> DroolsGroupingTriConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractBiConstraintStream<Solution_, A, B> parent, BiFunction<A, B, NewA> groupKeyAMapping,
            BiConstraintCollector<A, B, __, NewB> collectorB, BiConstraintCollector<A, B, ___, NewC> collectorC) {
        super(constraintFactory);
        throw new UnsupportedOperationException();
    }

    public <A, B, C, __, ___> DroolsGroupingTriConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractTriConstraintStream<Solution_, A, B, C> parent, TriFunction<A, B, C, NewA> groupKeyAMapping,
            TriConstraintCollector<A, B, C, __, NewB> collectorB, TriConstraintCollector<A, B, C, ___, NewC> collectorC) {
        super(constraintFactory);
        throw new UnsupportedOperationException();
    }

    public <A, B, C, D, __, ___> DroolsGroupingTriConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractQuadConstraintStream<Solution_, A, B, C, D> parent,
            QuadFunction<A, B, C, D, NewA> groupKeyAMapping, QuadConstraintCollector<A, B, C, D, __, NewB> collectorB,
            QuadConstraintCollector<A, B, C, D, ___, NewC> collectorC) {
        super(constraintFactory);
        throw new UnsupportedOperationException();
    }

    public <A, __> DroolsGroupingTriConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractUniConstraintStream<Solution_, A> parent, Function<A, NewA> groupKeyAMapping,
            Function<A, NewB> groupKeyBMapping, UniConstraintCollector<A, __, NewC> collector) {
        super(constraintFactory);
        this.leftHandSide = parent.getLeftHandSide().andGroupBy(groupKeyAMapping, groupKeyBMapping, collector);
    }

    public <A, B, __> DroolsGroupingTriConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractBiConstraintStream<Solution_, A, B> parent, BiFunction<A, B, NewA> groupKeyAMapping,
            BiFunction<A, B, NewB> groupKeyBMapping, BiConstraintCollector<A, B, __, NewC> collector) {
        super(constraintFactory);
        this.leftHandSide = parent.getLeftHandSide().andGroupBy(groupKeyAMapping, groupKeyBMapping, collector);
    }

    public <A, B, C, __> DroolsGroupingTriConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractTriConstraintStream<Solution_, A, B, C> parent, TriFunction<A, B, C, NewA> groupKeyAMapping,
            TriFunction<A, B, C, NewB> groupKeyBMapping, TriConstraintCollector<A, B, C, __, NewC> collector) {
        super(constraintFactory);
        this.leftHandSide = parent.getLeftHandSide().andGroupBy(groupKeyAMapping, groupKeyBMapping, collector);
    }

    public <A, B, C, D, __> DroolsGroupingTriConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractQuadConstraintStream<Solution_, A, B, C, D> parent,
            QuadFunction<A, B, C, D, NewA> groupKeyAMapping, QuadFunction<A, B, C, D, NewB> groupKeyBMapping,
            QuadConstraintCollector<A, B, C, D, __, NewC> collector) {
        super(constraintFactory);
        this.leftHandSide = parent.getLeftHandSide().andGroupBy(groupKeyAMapping, groupKeyBMapping, collector);
    }

    // ************************************************************************
    // Pattern creation
    // ************************************************************************

    @Override
    public TriLeftHandSide<NewA, NewB, NewC> getLeftHandSide() {
        return leftHandSide;
    }

    @Override
    public String toString() {
        return "TriGroup() with " + getChildStreams().size() + " children";
    }
}
