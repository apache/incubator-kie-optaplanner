package org.optaplanner.constraint.streams.bavet.quad;

import org.optaplanner.constraint.streams.bavet.common.AbstractUnindexedJoinNode;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.tri.TriTuple;
import org.optaplanner.constraint.streams.bavet.uni.UniTuple;

final class UnindexedJoinQuadNode<A, B, C, D>
        extends AbstractUnindexedJoinNode<TriTuple<A, B, C>, D, QuadTuple<A, B, C, D>, QuadTupleImpl<A, B, C, D>> {

    private final int outputStoreSize;

    public UnindexedJoinQuadNode(
            int inputStoreIndexLeftEntry, int inputStoreIndexLeftOutTupleList,
            int inputStoreIndexRightEntry, int inputStoreIndexRightOutTupleList,
            TupleLifecycle<QuadTuple<A, B, C, D>> nextNodesTupleLifecycle, int outputStoreSize,
            int outputStoreIndexLeftOutEntry, int outputStoreIndexRightOutEntry) {
        super(inputStoreIndexLeftEntry, inputStoreIndexLeftOutTupleList,
                inputStoreIndexRightEntry, inputStoreIndexRightOutTupleList,
                nextNodesTupleLifecycle,
                outputStoreIndexLeftOutEntry, outputStoreIndexRightOutEntry);
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected void setOutTupleLeftFacts(QuadTupleImpl<A, B, C, D> outTuple, TriTuple<A, B, C> leftTuple) {
        outTuple.factA = leftTuple.getFactA();
        outTuple.factB = leftTuple.getFactB();
        outTuple.factC = leftTuple.getFactC();
    }

    @Override
    protected void setOutTupleRightFact(QuadTupleImpl<A, B, C, D> outTuple, UniTuple<D> rightTuple) {
        outTuple.factD = rightTuple.getFactA();
    }

    @Override
    protected QuadTupleImpl<A, B, C, D> createOutTuple(TriTuple<A, B, C> leftTuple, UniTuple<D> rightTuple) {
        return new QuadTupleImpl<>(leftTuple.getFactA(), leftTuple.getFactB(), leftTuple.getFactC(), rightTuple.getFactA(),
                outputStoreSize);
    }

}
