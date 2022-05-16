/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.constraint.streams.bavet.quad;

import java.util.function.Consumer;

import org.optaplanner.constraint.streams.bavet.BavetConstraintFactory;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.score.stream.quad.QuadConstraintCollector;

final class BavetGroupBridge1Mapping3CollectorQuadConstraintStream<Solution_, A, B, C, D, NewA, ResultContainerB_, NewB, ResultContainerC_, NewC, ResultContainerD_, NewD>
        extends BavetAbstractQuadGroupBridgeQuadConstraintStream<Solution_, A, B, C, D, NewA, NewB, NewC, NewD> {

    private final QuadFunction<A, B, C, D, NewA> groupKeyMapping;
    private final QuadConstraintCollector<A, B, C, D, ResultContainerB_, NewB> collectorB;
    private final QuadConstraintCollector<A, B, C, D, ResultContainerC_, NewC> collectorC;
    private final QuadConstraintCollector<A, B, C, D, ResultContainerD_, NewD> collectorD;

    public BavetGroupBridge1Mapping3CollectorQuadConstraintStream(BavetConstraintFactory<Solution_> constraintFactory,
            BavetAbstractQuadConstraintStream<Solution_, A, B, C, D> parent, QuadFunction<A, B, C, D, NewA> groupKeyMapping,
            QuadConstraintCollector<A, B, C, D, ResultContainerB_, NewB> collectorB,
            QuadConstraintCollector<A, B, C, D, ResultContainerC_, NewC> collectorC,
            QuadConstraintCollector<A, B, C, D, ResultContainerD_, NewD> collectorD) {
        super(constraintFactory, parent);
        this.groupKeyMapping = groupKeyMapping;
        this.collectorB = collectorB;
        this.collectorC = collectorC;
        this.collectorD = collectorD;
    }

    // ************************************************************************
    // Node creation
    // ************************************************************************

    @Override
    protected AbstractGroupQuadNode<A, B, C, D, QuadTuple<NewA, NewB, NewC, NewD>, ?, ?, ?> createNode(int inputStoreIndex,
            Consumer<QuadTuple<NewA, NewB, NewC, NewD>> insert, Consumer<QuadTuple<NewA, NewB, NewC, NewD>> retract,
            int outputStoreSize) {
        return new Group1Mapping3CollectorQuadNode<>(groupKeyMapping, inputStoreIndex, collectorB, collectorC, collectorD,
                insert, retract, outputStoreSize);
    }
}
