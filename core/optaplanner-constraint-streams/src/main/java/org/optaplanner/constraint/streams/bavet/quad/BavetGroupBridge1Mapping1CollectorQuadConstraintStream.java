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
import org.optaplanner.constraint.streams.bavet.bi.BiTuple;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.score.stream.quad.QuadConstraintCollector;

final class BavetGroupBridge1Mapping1CollectorQuadConstraintStream<Solution_, A, B, C, D, NewA, ResultContainer_, NewB>
        extends BavetAbstractBiGroupBridgeQuadConstraintStream<Solution_, A, B, C, D, NewA, NewB> {

    private final QuadFunction<A, B, C, D, NewA> groupKeyMapping;
    private final QuadConstraintCollector<A, B, C, D, ResultContainer_, NewB> collector;

    public BavetGroupBridge1Mapping1CollectorQuadConstraintStream(BavetConstraintFactory<Solution_> constraintFactory,
            BavetAbstractQuadConstraintStream<Solution_, A, B, C, D> parent,
            QuadFunction<A, B, C, D, NewA> groupKeyMapping,
            QuadConstraintCollector<A, B, C, D, ResultContainer_, NewB> collector) {
        super(constraintFactory, parent);
        this.groupKeyMapping = groupKeyMapping;
        this.collector = collector;
    }

    // ************************************************************************
    // Node creation
    // ************************************************************************

    @Override
    protected AbstractGroupQuadNode<A, B, C, D, BiTuple<NewA, NewB>, ?, ?, ?> createNode(int inputStoreIndex,
            Consumer<BiTuple<NewA, NewB>> insert, Consumer<BiTuple<NewA, NewB>> retract, int outputStoreSize) {
        return new Group1Mapping1CollectorQuadNode<>(
                groupKeyMapping, inputStoreIndex, collector,
                insert, retract, outputStoreSize);
    }

}
