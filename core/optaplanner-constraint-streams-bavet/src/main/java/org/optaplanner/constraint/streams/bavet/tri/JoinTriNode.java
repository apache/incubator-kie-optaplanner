package org.optaplanner.constraint.streams.bavet.tri;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.bi.BiTupleImpl;
import org.optaplanner.constraint.streams.bavet.common.AbstractJoinNode;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.index.IndexProperties;
import org.optaplanner.constraint.streams.bavet.common.index.Indexer;
import org.optaplanner.constraint.streams.bavet.uni.UniTupleImpl;

final class JoinTriNode<A, B, C> extends AbstractJoinNode<BiTupleImpl<A, B>, C, TriTupleImpl<A, B, C>> {

    private final BiFunction<A, B, IndexProperties> mappingAB;
    private final int outputStoreSize;

    public JoinTriNode(BiFunction<A, B, IndexProperties> mappingAB, Function<C, IndexProperties> mappingC,
            int inputStoreIndexAB, int inputStoreIndexC,
            TupleLifecycle<TriTupleImpl<A, B, C>> nextNodesTupleLifecycle,
            int outputStoreSize,
            Indexer<BiTupleImpl<A, B>, Map<UniTupleImpl<C>, TriTupleImpl<A, B, C>>> indexerAB,
            Indexer<UniTupleImpl<C>, Map<BiTupleImpl<A, B>, TriTupleImpl<A, B, C>>> indexerC) {
        super(mappingC, inputStoreIndexAB, inputStoreIndexC, nextNodesTupleLifecycle, indexerAB, indexerC);
        this.mappingAB = mappingAB;
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected IndexProperties createIndexPropertiesLeft(BiTupleImpl<A, B> leftTuple) {
        return mappingAB.apply(leftTuple.factA, leftTuple.factB);
    }

    @Override
    protected TriTupleImpl<A, B, C> createOutTuple(BiTupleImpl<A, B> leftTuple, UniTupleImpl<C> rightTuple) {
        return new TriTupleImpl<>(leftTuple.factA, leftTuple.factB, rightTuple.factA, outputStoreSize);
    }

    @Override
    protected void updateOutTupleLeft(TriTupleImpl<A, B, C> outTuple, BiTupleImpl<A, B> leftTuple) {
        outTuple.factA = leftTuple.factA;
        outTuple.factB = leftTuple.factB;
    }

    @Override
    protected void updateOutTupleRight(TriTupleImpl<A, B, C> outTuple, UniTupleImpl<C> rightTuple) {
        outTuple.factC = rightTuple.factA;
    }

    @Override
    public String toString() {
        return "JoinTriNode";
    }

}
