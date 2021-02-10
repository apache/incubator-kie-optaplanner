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

package org.optaplanner.core.impl.score.stream.drools.quad;

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
import org.optaplanner.core.impl.score.stream.drools.common.QuadLeftHandSide;
import org.optaplanner.core.impl.score.stream.drools.tri.DroolsAbstractTriConstraintStream;
import org.optaplanner.core.impl.score.stream.drools.uni.DroolsAbstractUniConstraintStream;

public final class DroolsGroupingQuadConstraintStream<Solution_, NewA, NewB, NewC, NewD>
        extends DroolsAbstractQuadConstraintStream<Solution_, NewA, NewB, NewC, NewD> {

    private final QuadLeftHandSide<NewA, NewB, NewC, NewD> leftHandSide;

    public <A, __, ___, ____> DroolsGroupingQuadConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractUniConstraintStream<Solution_, A> parent, Function<A, NewA> groupKeyAMapping,
            UniConstraintCollector<A, __, NewB> collectorB, UniConstraintCollector<A, ___, NewC> collectorC,
            UniConstraintCollector<A, ____, NewD> collectorD) {
        super(constraintFactory);
        this.leftHandSide = parent.getLeftHandSide().andGroupBy(groupKeyAMapping, collectorB, collectorC, collectorD);
    }

    public <A, B, __, ___, ____> DroolsGroupingQuadConstraintStream(
            DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractBiConstraintStream<Solution_, A, B> parent, BiFunction<A, B, NewA> groupKeyAMapping,
            BiConstraintCollector<A, B, __, NewB> collectorB, BiConstraintCollector<A, B, ___, NewC> collectorC,
            BiConstraintCollector<A, B, ____, NewD> collectorD) {
        super(constraintFactory);
        this.leftHandSide = parent.getLeftHandSide().andGroupBy(groupKeyAMapping, collectorB, collectorC, collectorD);
    }

    public <A, B, C, __, ___, ____> DroolsGroupingQuadConstraintStream(
            DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractTriConstraintStream<Solution_, A, B, C> parent, TriFunction<A, B, C, NewA> groupKeyAMapping,
            TriConstraintCollector<A, B, C, __, NewB> collectorB, TriConstraintCollector<A, B, C, ___, NewC> collectorC,
            TriConstraintCollector<A, B, C, ____, NewD> collectorD) {
        super(constraintFactory);
        this.leftHandSide = parent.getLeftHandSide().andGroupBy(groupKeyAMapping, collectorB, collectorC, collectorD);
    }

    public <A, B, C, D, __, ___, ____> DroolsGroupingQuadConstraintStream(
            DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractQuadConstraintStream<Solution_, A, B, C, D> parent,
            QuadFunction<A, B, C, D, NewA> groupKeyAMapping, QuadConstraintCollector<A, B, C, D, __, NewB> collectorB,
            QuadConstraintCollector<A, B, C, D, ___, NewC> collectorC,
            QuadConstraintCollector<A, B, C, D, ____, NewD> collectorD) {
        super(constraintFactory);
        this.leftHandSide = parent.getLeftHandSide().andGroupBy(groupKeyAMapping, collectorB, collectorC, collectorD);
    }

    public <A, __, ___> DroolsGroupingQuadConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractUniConstraintStream<Solution_, A> parent, Function<A, NewA> groupKeyAMapping,
            Function<A, NewB> groupKeyBMapping, UniConstraintCollector<A, __, NewC> collectorC,
            UniConstraintCollector<A, ___, NewD> collectorD) {
        super(constraintFactory);
        this.leftHandSide = parent.getLeftHandSide().andGroupBy(groupKeyAMapping, groupKeyBMapping, collectorC, collectorD);
    }

    public <A, B, __, ___> DroolsGroupingQuadConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractBiConstraintStream<Solution_, A, B> parent, BiFunction<A, B, NewA> groupKeyAMapping,
            BiFunction<A, B, NewB> groupKeyBMapping, BiConstraintCollector<A, B, __, NewC> collectorC,
            BiConstraintCollector<A, B, ___, NewD> collectorD) {
        super(constraintFactory);
        this.leftHandSide = parent.getLeftHandSide().andGroupBy(groupKeyAMapping, groupKeyBMapping, collectorC, collectorD);
    }

    public <A, B, C, __, ___> DroolsGroupingQuadConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractTriConstraintStream<Solution_, A, B, C> parent, TriFunction<A, B, C, NewA> groupKeyAMapping,
            TriFunction<A, B, C, NewB> groupKeyBMapping,
            TriConstraintCollector<A, B, C, __, NewC> collectorC,
            TriConstraintCollector<A, B, C, ___, NewD> collectorD) {
        super(constraintFactory);
        this.leftHandSide = parent.getLeftHandSide().andGroupBy(groupKeyAMapping, groupKeyBMapping, collectorC, collectorD);
    }

    public <A, B, C, D, __, ___> DroolsGroupingQuadConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractQuadConstraintStream<Solution_, A, B, C, D> parent,
            QuadFunction<A, B, C, D, NewA> groupKeyAMapping,
            QuadFunction<A, B, C, D, NewB> groupKeyBMapping,
            QuadConstraintCollector<A, B, C, D, __, NewC> collectorC,
            QuadConstraintCollector<A, B, C, D, ___, NewD> collectorD) {
        super(constraintFactory);
        this.leftHandSide = parent.getLeftHandSide().andGroupBy(groupKeyAMapping, groupKeyBMapping, collectorC, collectorD);
    }

    public <A, __, ___, ____, _____> DroolsGroupingQuadConstraintStream(
            DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractUniConstraintStream<Solution_, A> parent, UniConstraintCollector<A, __, NewA> collectorA,
            UniConstraintCollector<A, ___, NewB> collectorB, UniConstraintCollector<A, ____, NewC> collectorC,
            UniConstraintCollector<A, _____, NewD> collectorD) {
        super(constraintFactory);
        this.leftHandSide = parent.getLeftHandSide().andGroupBy(collectorA, collectorB, collectorC, collectorD);
    }

    public <A, B, __, ___, ____, _____> DroolsGroupingQuadConstraintStream(
            DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractBiConstraintStream<Solution_, A, B> parent,
            BiConstraintCollector<A, B, __, NewA> collectorA, BiConstraintCollector<A, B, ___, NewB> collectorB,
            BiConstraintCollector<A, B, ____, NewC> collectorC, BiConstraintCollector<A, B, _____, NewD> collectorD) {
        super(constraintFactory);
        this.leftHandSide = parent.getLeftHandSide().andGroupBy(collectorA, collectorB, collectorC, collectorD);
    }

    public <A, B, C, __, ___, ____, _____> DroolsGroupingQuadConstraintStream(
            DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractTriConstraintStream<Solution_, A, B, C> parent,
            TriConstraintCollector<A, B, C, __, NewA> collectorA, TriConstraintCollector<A, B, C, ___, NewB> collectorB,
            TriConstraintCollector<A, B, C, ____, NewC> collectorC,
            TriConstraintCollector<A, B, C, _____, NewD> collectorD) {
        super(constraintFactory);
        this.leftHandSide = parent.getLeftHandSide().andGroupBy(collectorA, collectorB, collectorC, collectorD);
    }

    public <A, B, C, D, __, ___, ____, _____> DroolsGroupingQuadConstraintStream(
            DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractQuadConstraintStream<Solution_, A, B, C, D> parent,
            QuadConstraintCollector<A, B, C, D, __, NewA> collectorA,
            QuadConstraintCollector<A, B, C, D, ___, NewB> collectorB,
            QuadConstraintCollector<A, B, C, D, ____, NewC> collectorC,
            QuadConstraintCollector<A, B, C, D, _____, NewD> collectorD) {
        super(constraintFactory);
        this.leftHandSide = parent.getLeftHandSide().andGroupBy(collectorA, collectorB, collectorC, collectorD);
    }

    // ************************************************************************
    // Pattern creation
    // ************************************************************************

    @Override
    public QuadLeftHandSide<NewA, NewB, NewC, NewD> getLeftHandSide() {
        return leftHandSide;
    }

    // ************************************************************************
    // Getters/setters
    // ************************************************************************

    @Override
    public String toString() {
        return "QuadGroupBy() with " + getChildStreams().size() + " children";
    }

}
