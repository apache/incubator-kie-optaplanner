/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.constraint.streams.bavet.bi;

import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.tri.TriTuple;
import org.optaplanner.constraint.streams.bavet.tri.TriTupleImpl;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;
import org.optaplanner.core.api.score.stream.bi.BiConstraintCollector;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.impl.util.Triple;

final class Group0Mapping3CollectorBiNode<OldA, OldB, A, B, C, ResultContainerA_, ResultContainerB_, ResultContainerC_>
        extends AbstractGroupBiNode<OldA, OldB, TriTuple<A, B, C>, TriTupleImpl<A, B, C>, Void, Object, Triple<A, B, C>> {

    private final int outputStoreSize;

    public Group0Mapping3CollectorBiNode(int groupStoreIndex, int undoStoreIndex,
            BiConstraintCollector<OldA, OldB, ResultContainerA_, A> collectorA,
            BiConstraintCollector<OldA, OldB, ResultContainerB_, B> collectorB,
            BiConstraintCollector<OldA, OldB, ResultContainerC_, C> collectorC,
            TupleLifecycle<TriTuple<A, B, C>> nextNodesTupleLifecycle, int outputStoreSize, EnvironmentMode environmentMode) {
        super(groupStoreIndex, undoStoreIndex, null, mergeCollectors(collectorA, collectorB, collectorC),
                nextNodesTupleLifecycle, environmentMode);
        this.outputStoreSize = outputStoreSize;
    }

    static <OldA, OldB, A, B, C, ResultContainerA_, ResultContainerB_, ResultContainerC_>
            BiConstraintCollector<OldA, OldB, Object, Triple<A, B, C>> mergeCollectors(
                    BiConstraintCollector<OldA, OldB, ResultContainerA_, A> collectorA,
                    BiConstraintCollector<OldA, OldB, ResultContainerB_, B> collectorB,
                    BiConstraintCollector<OldA, OldB, ResultContainerC_, C> collectorC) {
        return (BiConstraintCollector<OldA, OldB, Object, Triple<A, B, C>>) ConstraintCollectors.compose(collectorA, collectorB,
                collectorC, Triple::of);
    }

    @Override
    protected TriTupleImpl<A, B, C> createOutTuple(Void groupKey) {
        return new TriTupleImpl<>(null, null, null, outputStoreSize);
    }

    @Override
    protected void updateOutTupleToResult(TriTupleImpl<A, B, C> outTuple, Triple<A, B, C> result) {
        outTuple.factA = result.getA();
        outTuple.factB = result.getB();
        outTuple.factC = result.getC();
    }

}
