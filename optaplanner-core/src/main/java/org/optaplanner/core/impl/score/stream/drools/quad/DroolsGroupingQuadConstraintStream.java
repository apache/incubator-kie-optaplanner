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

    public <A> DroolsGroupingQuadConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractUniConstraintStream<Solution_, A> parent, Function<A, NewA> groupKeyAMapping,
            UniConstraintCollector<A, ?, NewB> collectorB, UniConstraintCollector<A, ?, NewC> collectorC,
            UniConstraintCollector<A, ?, NewD> collectorD) {
        super(constraintFactory);
        this.leftHandSide = parent.getLeftHandSide().andGroupBy(groupKeyAMapping, collectorB, collectorC, collectorD);
    }

    public <A, B> DroolsGroupingQuadConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractBiConstraintStream<Solution_, A, B> parent, BiFunction<A, B, NewA> groupKeyAMapping,
            BiConstraintCollector<A, B, ?, NewB> collectorB, BiConstraintCollector<A, B, ?, NewC> collectorC,
            BiConstraintCollector<A, B, ?, NewD> collectorD) {
        super(constraintFactory);
        this.leftHandSide = parent.getLeftHandSide().andGroupBy(groupKeyAMapping, collectorB, collectorC, collectorD);
    }

    public <A, B, C> DroolsGroupingQuadConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractTriConstraintStream<Solution_, A, B, C> parent, TriFunction<A, B, C, NewA> groupKeyAMapping,
            TriConstraintCollector<A, B, C, ?, NewB> collectorB, TriConstraintCollector<A, B, C, ?, NewC> collectorC,
            TriConstraintCollector<A, B, C, ?, NewD> collectorD) {
        super(constraintFactory);
        this.leftHandSide = parent.getLeftHandSide().andGroupBy(groupKeyAMapping, collectorB, collectorC, collectorD);
    }

    public <A, B, C, D> DroolsGroupingQuadConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractQuadConstraintStream<Solution_, A, B, C, D> parent,
            QuadFunction<A, B, C, D, NewA> groupKeyAMapping, QuadConstraintCollector<A, B, C, D, ?, NewB> collectorB,
            QuadConstraintCollector<A, B, C, D, ?, NewC> collectorC,
            QuadConstraintCollector<A, B, C, D, ?, NewD> collectorD) {
        super(constraintFactory);
        this.leftHandSide = parent.getLeftHandSide().andGroupBy(groupKeyAMapping, collectorB, collectorC, collectorD);
    }

    public <A> DroolsGroupingQuadConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractUniConstraintStream<Solution_, A> parent, Function<A, NewA> groupKeyAMapping,
            Function<A, NewB> groupKeyBMapping, UniConstraintCollector<A, ?, NewC> collectorC,
            UniConstraintCollector<A, ?, NewD> collectorD) {
        super(constraintFactory);
        this.leftHandSide = parent.getLeftHandSide().andGroupBy(groupKeyAMapping, groupKeyBMapping, collectorC, collectorD);
    }

    public <A, B> DroolsGroupingQuadConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractBiConstraintStream<Solution_, A, B> parent, BiFunction<A, B, NewA> groupKeyAMapping,
            BiFunction<A, B, NewB> groupKeyBMapping, BiConstraintCollector<A, B, ?, NewC> collectorC,
            BiConstraintCollector<A, B, ?, NewD> collectorD) {
        super(constraintFactory);
        this.leftHandSide = parent.getLeftHandSide().andGroupBy(groupKeyAMapping, groupKeyBMapping, collectorC, collectorD);
    }

    public <A, B, C> DroolsGroupingQuadConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractTriConstraintStream<Solution_, A, B, C> parent, TriFunction<A, B, C, NewA> groupKeyAMapping,
            TriFunction<A, B, C, NewB> groupKeyBMapping, TriConstraintCollector<A, B, C, ?, NewC> collectorC,
            TriConstraintCollector<A, B, C, ?, NewD> collectorD) {
        super(constraintFactory);
        this.leftHandSide = parent.getLeftHandSide().andGroupBy(groupKeyAMapping, groupKeyBMapping, collectorC, collectorD);
    }

    public <A, B, C, D> DroolsGroupingQuadConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractQuadConstraintStream<Solution_, A, B, C, D> parent,
            QuadFunction<A, B, C, D, NewA> groupKeyAMapping, QuadFunction<A, B, C, D, NewB> groupKeyBMapping,
            QuadConstraintCollector<A, B, C, D, ?, NewC> collectorC,
            QuadConstraintCollector<A, B, C, D, ?, NewD> collectorD) {
        super(constraintFactory);
        this.leftHandSide = parent.getLeftHandSide().andGroupBy(groupKeyAMapping, groupKeyBMapping, collectorC, collectorD);
    }

    public <A> DroolsGroupingQuadConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractUniConstraintStream<Solution_, A> parent, UniConstraintCollector<A, ?, NewA> collectorA,
            UniConstraintCollector<A, ?, NewB> collectorB, UniConstraintCollector<A, ?, NewC> collectorC,
            UniConstraintCollector<A, ?, NewD> collectorD) {
        super(constraintFactory);
        this.leftHandSide = parent.getLeftHandSide().andGroupBy(collectorA, collectorB, collectorC, collectorD);
    }

    public <A, B> DroolsGroupingQuadConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractBiConstraintStream<Solution_, A, B> parent,
            BiConstraintCollector<A, B, ?, NewA> collectorA, BiConstraintCollector<A, B, ?, NewB> collectorB,
            BiConstraintCollector<A, B, ?, NewC> collectorC, BiConstraintCollector<A, B, ?, NewD> collectorD) {
        super(constraintFactory);
        this.leftHandSide = parent.getLeftHandSide().andGroupBy(collectorA, collectorB, collectorC, collectorD);
    }

    public <A, B, C> DroolsGroupingQuadConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractTriConstraintStream<Solution_, A, B, C> parent,
            TriConstraintCollector<A, B, C, ?, NewA> collectorA, TriConstraintCollector<A, B, C, ?, NewB> collectorB,
            TriConstraintCollector<A, B, C, ?, NewC> collectorC,
            TriConstraintCollector<A, B, C, ?, NewD> collectorD) {
        super(constraintFactory);
        this.leftHandSide = parent.getLeftHandSide().andGroupBy(collectorA, collectorB, collectorC, collectorD);
    }

    public <A, B, C, D> DroolsGroupingQuadConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractQuadConstraintStream<Solution_, A, B, C, D> parent,
            QuadConstraintCollector<A, B, C, D, ?, NewA> collectorA,
            QuadConstraintCollector<A, B, C, D, ?, NewB> collectorB,
            QuadConstraintCollector<A, B, C, D, ?, NewC> collectorC,
            QuadConstraintCollector<A, B, C, D, ?, NewD> collectorD) {
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
