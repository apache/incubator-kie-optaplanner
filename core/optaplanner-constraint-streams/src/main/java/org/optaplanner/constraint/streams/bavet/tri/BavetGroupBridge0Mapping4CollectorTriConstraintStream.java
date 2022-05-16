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

package org.optaplanner.constraint.streams.bavet.tri;

import java.util.function.Consumer;

import org.optaplanner.constraint.streams.bavet.BavetConstraintFactory;
import org.optaplanner.constraint.streams.bavet.quad.QuadTuple;
import org.optaplanner.core.api.score.stream.tri.TriConstraintCollector;

final class BavetGroupBridge0Mapping4CollectorTriConstraintStream<Solution_, A, B, C, ResultContainerA_, NewA, ResultContainerB_, NewB, ResultContainerC_, NewC, ResultContainerD_, NewD>
        extends BavetAbstractQuadGroupBridgeTriConstraintStream<Solution_, A, B, C, NewA, NewB, NewC, NewD> {

    private final TriConstraintCollector<A, B, C, ResultContainerA_, NewA> collectorA;
    private final TriConstraintCollector<A, B, C, ResultContainerB_, NewB> collectorB;
    private final TriConstraintCollector<A, B, C, ResultContainerC_, NewC> collectorC;
    private final TriConstraintCollector<A, B, C, ResultContainerD_, NewD> collectorD;

    public BavetGroupBridge0Mapping4CollectorTriConstraintStream(BavetConstraintFactory<Solution_> constraintFactory,
            BavetAbstractTriConstraintStream<Solution_, A, B, C> parent,
            TriConstraintCollector<A, B, C, ResultContainerA_, NewA> collectorA,
            TriConstraintCollector<A, B, C, ResultContainerB_, NewB> collectorB,
            TriConstraintCollector<A, B, C, ResultContainerC_, NewC> collectorC,
            TriConstraintCollector<A, B, C, ResultContainerD_, NewD> collectorD) {
        super(constraintFactory, parent);
        this.collectorA = collectorA;
        this.collectorB = collectorB;
        this.collectorC = collectorC;
        this.collectorD = collectorD;
    }

    // ************************************************************************
    // Node creation
    // ************************************************************************

    @Override
    protected AbstractGroupTriNode<A, B, C, QuadTuple<NewA, NewB, NewC, NewD>, ?, ?, ?> createNode(int inputStoreIndex,
            Consumer<QuadTuple<NewA, NewB, NewC, NewD>> insert, Consumer<QuadTuple<NewA, NewB, NewC, NewD>> retract,
            int outputStoreSize) {
        return new Group0Mapping4CollectorTriNode<>(inputStoreIndex, collectorA, collectorB, collectorC, collectorD,
                insert, retract, outputStoreSize);
    }
}
