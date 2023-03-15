package org.optaplanner.constraint.streams.bavet.tri;

import org.optaplanner.constraint.streams.bavet.common.AbstractUnindexedJoinNode;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.tuple.BiTuple;
import org.optaplanner.constraint.streams.bavet.common.tuple.TriTuple;
import org.optaplanner.constraint.streams.bavet.common.tuple.TriTupleImpl;
import org.optaplanner.constraint.streams.bavet.common.tuple.UniTuple;
import org.optaplanner.core.api.function.TriPredicate;

final class UnindexedJoinTriNode<A, B, C>
        extends AbstractUnindexedJoinNode<BiTuple<A, B>, C, TriTuple<A, B, C>, TriTupleImpl<A, B, C>> {

    private final TriPredicate<A, B, C> filtering;
    private final int outputStoreSize;

    public UnindexedJoinTriNode(
            int inputStoreIndexLeftEntry, int inputStoreIndexLeftOutTupleList,
            int inputStoreIndexRightEntry, int inputStoreIndexRightOutTupleList,
            TupleLifecycle<TriTuple<A, B, C>> nextNodesTupleLifecycle, TriPredicate<A, B, C> filtering,
            int outputStoreSize,
            int outputStoreIndexLeftOutEntry, int outputStoreIndexRightOutEntry) {
        super(inputStoreIndexLeftEntry, inputStoreIndexLeftOutTupleList,
                inputStoreIndexRightEntry, inputStoreIndexRightOutTupleList,
                nextNodesTupleLifecycle, filtering != null,
                outputStoreIndexLeftOutEntry, outputStoreIndexRightOutEntry);
        this.filtering = filtering;
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected TriTupleImpl<A, B, C> createOutTuple(BiTuple<A, B> leftTuple, UniTuple<C> rightTuple) {
        return new TriTupleImpl<>(leftTuple.getA(), leftTuple.getB(), rightTuple.getA(), outputStoreSize);
    }

    @Override
    protected void setOutTupleLeftFacts(TriTupleImpl<A, B, C> outTuple, BiTuple<A, B> leftTuple) {
        outTuple.setA(leftTuple.getA());
        outTuple.setB(leftTuple.getB());
    }

    @Override
    protected void setOutTupleRightFact(TriTupleImpl<A, B, C> outTuple, UniTuple<C> rightTuple) {
        outTuple.setC(rightTuple.getA());
    }

    @Override
    protected boolean testFiltering(BiTuple<A, B> leftTuple, UniTuple<C> rightTuple) {
        return filtering.test(leftTuple.getA(), leftTuple.getB(), rightTuple.getA());
    }

}
