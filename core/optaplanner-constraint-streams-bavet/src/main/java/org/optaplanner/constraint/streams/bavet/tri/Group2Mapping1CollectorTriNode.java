package org.optaplanner.constraint.streams.bavet.tri;

import static org.optaplanner.constraint.streams.bavet.tri.Group2Mapping0CollectorTriNode.createGroupKey;

import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.tuple.TriTuple;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.stream.tri.TriConstraintCollector;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.impl.util.Pair;

final class Group2Mapping1CollectorTriNode<OldA, OldB, OldC, A, B, C, ResultContainer_>
        extends
        AbstractGroupTriNode<OldA, OldB, OldC, TriTuple<A, B, C>, Pair<A, B>, ResultContainer_, C> {

    private final int outputStoreSize;

    public Group2Mapping1CollectorTriNode(TriFunction<OldA, OldB, OldC, A> groupKeyMappingA,
            TriFunction<OldA, OldB, OldC, B> groupKeyMappingB, int groupStoreIndex, int undoStoreIndex,
            TriConstraintCollector<OldA, OldB, OldC, ResultContainer_, C> collector,
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
