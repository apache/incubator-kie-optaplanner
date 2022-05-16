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

final class BavetGroupBridge3Mapping0CollectorQuadConstraintStream<Solution_, A, B, C, D, NewA, NewB, NewC>
        extends BavetAbstractTriGroupBridgeQuadConstraintStream<Solution_, A, B, C, D, NewA, NewB, NewC> {

    QuadFunction<A, B, C, D, NewA> groupKeyMappingA;
    QuadFunction<A, B, C, D, NewB> groupKeyMappingB;
    QuadFunction<A, B, C, D, NewC> groupKeyMappingC;

    public BavetGroupBridge3Mapping0CollectorQuadConstraintStream(BavetConstraintFactory<Solution_> constraintFactory,
            BavetAbstractQuadConstraintStream<Solution_, A, B, C, D> parent,
            QuadFunction<A, B, C, D, NewA> groupKeyMappingA, QuadFunction<A, B, C, D, NewB> groupKeyMappingB,
            QuadFunction<A, B, C, D, NewC> groupKeyMappingC) {
        super(constraintFactory, parent);
        this.groupKeyMappingA = groupKeyMappingA;
        this.groupKeyMappingB = groupKeyMappingB;
        this.groupKeyMappingC = groupKeyMappingC;
    }

    // ************************************************************************
    // Node creation
    // ************************************************************************

    @Override
    protected AbstractGroupQuadNode<A, B, C, D, TriTuple<NewA, NewB, NewC>, ?, ?, ?> createNode(int inputStoreIndex,
            Consumer<TriTuple<NewA, NewB, NewC>> insert, Consumer<TriTuple<NewA, NewB, NewC>> retract, int outputStoreSize) {
        return new Group3Mapping0CollectorQuadNode<>(
                groupKeyMappingA, groupKeyMappingB, groupKeyMappingC, inputStoreIndex,
                insert, retract, outputStoreSize);
    }
}
