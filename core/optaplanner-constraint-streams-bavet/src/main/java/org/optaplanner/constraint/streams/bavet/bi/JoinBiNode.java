package org.optaplanner.constraint.streams.bavet.bi;

import java.util.Map;
import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.AbstractJoinNode;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.index.IndexProperties;
import org.optaplanner.constraint.streams.bavet.common.index.Indexer;
import org.optaplanner.constraint.streams.bavet.uni.UniTupleImpl;

final class JoinBiNode<A, B> extends AbstractJoinNode<UniTupleImpl<A>, B, BiTupleImpl<A, B>> {

    private final Function<A, IndexProperties> mappingA;
    private final int outputStoreSize;

    public JoinBiNode(Function<A, IndexProperties> mappingA, Function<B, IndexProperties> mappingB,
            int inputStoreIndexA, int inputStoreIndexB,
            TupleLifecycle<BiTupleImpl<A, B>> nextNodesTupleLifecycle,
            int outputStoreSize,
            Indexer<UniTupleImpl<A>, Map<UniTupleImpl<B>, BiTupleImpl<A, B>>> indexerA,
            Indexer<UniTupleImpl<B>, Map<UniTupleImpl<A>, BiTupleImpl<A, B>>> indexerB) {
        super(mappingB, inputStoreIndexA, inputStoreIndexB, nextNodesTupleLifecycle, indexerA, indexerB);
        this.mappingA = mappingA;
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected IndexProperties createIndexPropertiesLeft(UniTupleImpl<A> leftTuple) {
        return mappingA.apply(leftTuple.factA);
    }

    @Override
    protected BiTupleImpl<A, B> createOutTuple(UniTupleImpl<A> leftTuple, UniTupleImpl<B> rightTuple) {
        return new BiTupleImpl<>(leftTuple.factA, rightTuple.factA, outputStoreSize);
    }

    @Override
    protected void updateOutTupleLeft(BiTupleImpl<A, B> outTuple, UniTupleImpl<A> leftTuple) {
        outTuple.factA = leftTuple.factA;
    }

    @Override
    protected void updateOutTupleRight(BiTupleImpl<A, B> outTuple, UniTupleImpl<B> rightTuple) {
        outTuple.factB = rightTuple.factA;
    }

    @Override
    public String toString() {
        return "JoinBiNode";
    }

}
