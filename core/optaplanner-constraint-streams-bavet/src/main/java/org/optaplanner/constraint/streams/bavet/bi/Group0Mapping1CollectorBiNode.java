package org.optaplanner.constraint.streams.bavet.bi;

import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.uni.UniTuple;
import org.optaplanner.constraint.streams.bavet.uni.UniTupleImpl;
import org.optaplanner.core.api.score.stream.bi.BiConstraintCollector;
import org.optaplanner.core.config.solver.EnvironmentMode;

final class Group0Mapping1CollectorBiNode<OldA, OldB, A, ResultContainer_>
        extends AbstractGroupBiNode<OldA, OldB, UniTuple<A>, UniTupleImpl<A>, Void, ResultContainer_, A> {

    private final int outputStoreSize;

    public Group0Mapping1CollectorBiNode(int groupStoreIndex, int undoStoreIndex,
            BiConstraintCollector<OldA, OldB, ResultContainer_, A> collector,
            TupleLifecycle<UniTuple<A>> nextNodesTupleLifecycle, int outputStoreSize, EnvironmentMode environmentMode) {
        super(groupStoreIndex, undoStoreIndex, null, collector, nextNodesTupleLifecycle, environmentMode);
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected UniTupleImpl<A> createOutTuple(Void groupKey) {
        return new UniTupleImpl<>(null, outputStoreSize);
    }

    @Override
    protected void updateOutTupleToResult(UniTupleImpl<A> outTuple, A a) {
        outTuple.factA = a;
    }

}
