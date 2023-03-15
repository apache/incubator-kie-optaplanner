package org.optaplanner.constraint.streams.bavet.quad;

import static org.optaplanner.constraint.streams.bavet.quad.Group2Mapping0CollectorQuadNode.createGroupKey;

import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.tuple.TriTuple;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.score.stream.quad.QuadConstraintCollector;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.impl.util.Pair;

final class Group2Mapping1CollectorQuadNode<OldA, OldB, OldC, OldD, A, B, C, ResultContainer_>
        extends
        AbstractGroupQuadNode<OldA, OldB, OldC, OldD, TriTuple<A, B, C>, Pair<A, B>, ResultContainer_, C> {

    private final int outputStoreSize;

    public Group2Mapping1CollectorQuadNode(QuadFunction<OldA, OldB, OldC, OldD, A> groupKeyMappingA,
            QuadFunction<OldA, OldB, OldC, OldD, B> groupKeyMappingB, int groupStoreIndex, int undoStoreIndex,
            QuadConstraintCollector<OldA, OldB, OldC, OldD, ResultContainer_, C> collector,
            TupleLifecycle<TriTuple<A, B, C>> nextNodesTupleLifecycle, int outputStoreSize, EnvironmentMode environmentMode) {
        super(groupStoreIndex, undoStoreIndex, tuple -> createGroupKey(groupKeyMappingA, groupKeyMappingB, tuple), collector,
                nextNodesTupleLifecycle, environmentMode);
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected TriTuple<A, B, C> createOutTuple(Pair<A, B> groupKey) {
        return TriTuple.of(groupKey.getKey(), groupKey.getValue(), null, outputStoreSize);
    }

    @Override
    protected void updateOutTupleToResult(TriTuple<A, B, C> outTuple, C c) {
        outTuple.setC(c);
    }

}
