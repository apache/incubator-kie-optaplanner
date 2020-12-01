/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.score.stream.bavet.bi;

import java.util.List;
import java.util.function.BiFunction;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.bi.BiConstraintCollector;
import org.optaplanner.core.impl.score.stream.bavet.BavetConstraintFactory;
import org.optaplanner.core.impl.score.stream.bavet.common.BavetNodeBuildPolicy;
import org.optaplanner.core.impl.score.stream.bavet.uni.BavetFromUniConstraintStream;

public final class BavetGroupBridgeBiConstraintStream<Solution_, A, B, NewA, ResultContainer_, NewB>
        extends BavetAbstractBiConstraintStream<Solution_, A, B> {

    private final BavetAbstractBiConstraintStream<Solution_, A, B> parent;
    private BavetGroupBiConstraintStream<Solution_, NewA, ResultContainer_, NewB> groupStream;
    private final BiFunction<A, B, NewA> groupKeyMapping;
    private final BiConstraintCollector<A, B, ResultContainer_, NewB> collector;

    public BavetGroupBridgeBiConstraintStream(BavetConstraintFactory<Solution_> constraintFactory,
            BavetAbstractBiConstraintStream<Solution_, A, B> parent,
            BiFunction<A, B, NewA> groupKeyMapping, BiConstraintCollector<A, B, ResultContainer_, NewB> collector) {
        super(constraintFactory);
        this.parent = parent;
        this.groupKeyMapping = groupKeyMapping;
        this.collector = collector;
    }

    public void setGroupStream(BavetGroupBiConstraintStream<Solution_, NewA, ResultContainer_, NewB> groupStream) {
        this.groupStream = groupStream;
    }

    @Override
    public List<BavetFromUniConstraintStream<Solution_, Object>> getFromStreamList() {
        return parent.getFromStreamList();
    }

    // ************************************************************************
    // Node creation
    // ************************************************************************

    @Override
    protected BavetAbstractBiNode<A, B> createNode(BavetNodeBuildPolicy<Solution_> buildPolicy,
            Score<?> constraintWeight, int nodeOrder, BavetAbstractBiNode<A, B> parentNode) {
        BavetGroupBiNode<NewA, ResultContainer_, NewB> groupNode = groupStream.createNodeChain(buildPolicy, constraintWeight,
                nodeOrder + 1, null);
        BavetGroupBridgeBiNode<A, B, NewA, ResultContainer_, NewB> node = new BavetGroupBridgeBiNode<>(
                buildPolicy.getSession(), nodeOrder, parentNode, groupKeyMapping, collector, groupNode);
        return node;
    }

    @Override
    protected void createChildNodeChains(BavetNodeBuildPolicy<Solution_> buildPolicy, Score<?> constraintWeight, int nodeOrder,
            BavetAbstractBiNode<A, B> node) {
        if (!childStreamList.isEmpty()) {
            throw new IllegalStateException("Impossible state: the stream (" + this
                    + ") has an non-empty childStreamList (" + childStreamList + ") but it's a groupBy bridge.");
        }
    }
}