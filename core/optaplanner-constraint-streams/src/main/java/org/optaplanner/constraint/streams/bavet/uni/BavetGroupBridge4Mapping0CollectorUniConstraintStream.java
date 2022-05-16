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

package org.optaplanner.constraint.streams.bavet.uni;

import java.util.function.Consumer;
import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.BavetConstraintFactory;
import org.optaplanner.constraint.streams.bavet.quad.QuadTuple;

final class BavetGroupBridge4Mapping0CollectorUniConstraintStream<Solution_, A, NewA, NewB, NewC, NewD>
        extends BavetAbstractQuadGroupBridgeUniConstraintStream<Solution_, A, NewA, NewB, NewC, NewD> {

    private final Function<A, NewA> groupKeyMappingA;
    private final Function<A, NewB> groupKeyMappingB;
    private final Function<A, NewC> groupKeyMappingC;
    private final Function<A, NewD> groupKeyMappingD;

    public BavetGroupBridge4Mapping0CollectorUniConstraintStream(BavetConstraintFactory<Solution_> constraintFactory,
            BavetAbstractUniConstraintStream<Solution_, A> parent,
            Function<A, NewA> groupKeyMappingA, Function<A, NewB> groupKeyMappingB,
            Function<A, NewC> groupKeyMappingC, Function<A, NewD> groupKeyMappingD) {
        super(constraintFactory, parent);
        this.groupKeyMappingA = groupKeyMappingA;
        this.groupKeyMappingB = groupKeyMappingB;
        this.groupKeyMappingC = groupKeyMappingC;
        this.groupKeyMappingD = groupKeyMappingD;
    }

    // ************************************************************************
    // Node creation
    // ************************************************************************

    @Override
    protected AbstractGroupUniNode<A, QuadTuple<NewA, NewB, NewC, NewD>, ?, ?, ?> createNode(int inputStoreIndex,
            Consumer<QuadTuple<NewA, NewB, NewC, NewD>> insert, Consumer<QuadTuple<NewA, NewB, NewC, NewD>> retract,
            int outputStoreSize) {
        return new Group4Mapping0CollectorUniNode<>(
                groupKeyMappingA, groupKeyMappingB, groupKeyMappingC, groupKeyMappingD, inputStoreIndex,
                insert, retract, outputStoreSize);
    }
}
