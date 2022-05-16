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
import org.optaplanner.constraint.streams.bavet.tri.TriTuple;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.score.stream.quad.QuadConstraintCollector;

final class BavetGroupBridge2Mapping1CollectorQuadConstraintStream<Solution_, A, B, C, D, NewA, NewB, ResultContainer_, NewC>
        extends BavetAbstractTriGroupBridgeQuadConstraintStream<Solution_, A, B, C, D, NewA, NewB, NewC> {

    private final QuadFunction<A, B, C, D, NewA> groupKeyMappingA;
    private final QuadFunction<A, B, C, D, NewB> groupKeyMappingB;
    private final QuadConstraintCollector<A, B, C, D, ResultContainer_, NewC> collector;

    public BavetGroupBridge2Mapping1CollectorQuadConstraintStream(BavetConstraintFactory<Solution_> constraintFactory,
            BavetAbstractQuadConstraintStream<Solution_, A, B, C, D> parent,
            QuadFunction<A, B, C, D, NewA> groupKeyMappingA, QuadFunction<A, B, C, D, NewB> groupKeyMappingB,
            QuadConstraintCollector<A, B, C, D, ResultContainer_, NewC> collector) {
        super(constraintFactory, parent);
        this.groupKeyMappingA = groupKeyMappingA;
        this.groupKeyMappingB = groupKeyMappingB;
        this.collector = collector;
    }

    // ************************************************************************
    // Node creation
    // ************************************************************************

    @Override
    protected AbstractGroupQuadNode<A, B, C, D, TriTuple<NewA, NewB, NewC>, ?, ?, ?> createNode(int inputStoreIndex,
            Consumer<TriTuple<NewA, NewB, NewC>> insert, Consumer<TriTuple<NewA, NewB, NewC>> retract,
            int outputStoreSize) {
        return new Group2Mapping1CollectorQuadNode<>(groupKeyMappingA, groupKeyMappingB, inputStoreIndex, collector,
                insert, retract, outputStoreSize);
    }
}
