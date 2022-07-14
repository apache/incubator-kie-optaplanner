package org.optaplanner.constraint.streams.bavet.quad;

import java.util.Map;
import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.AbstractJoinNode;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.index.IndexProperties;
import org.optaplanner.constraint.streams.bavet.common.index.Indexer;
import org.optaplanner.constraint.streams.bavet.tri.TriTupleImpl;
import org.optaplanner.constraint.streams.bavet.uni.UniTupleImpl;
import org.optaplanner.core.api.function.TriFunction;

final class JoinQuadNode<A, B, C, D> extends AbstractJoinNode<TriTupleImpl<A, B, C>, D, QuadTupleImpl<A, B, C, D>> {

    private final TriFunction<A, B, C, IndexProperties> mappingABC;
    private final int outputStoreSize;

    public JoinQuadNode(TriFunction<A, B, C, IndexProperties> mappingABC, Function<D, IndexProperties> mappingD,
            int inputStoreIndexAB, int inputStoreIndexC,
            TupleLifecycle<QuadTupleImpl<A, B, C, D>> nextNodesTupleLifecycle,
            int outputStoreSize,
            Indexer<TriTupleImpl<A, B, C>, Map<UniTupleImpl<D>, QuadTupleImpl<A, B, C, D>>> indexerABC,
            Indexer<UniTupleImpl<D>, Map<TriTupleImpl<A, B, C>, QuadTupleImpl<A, B, C, D>>> indexerD) {
        super(mappingD, inputStoreIndexAB, inputStoreIndexC, nextNodesTupleLifecycle, indexerABC, indexerD);
        this.mappingABC = mappingABC;
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected IndexProperties createIndexPropertiesLeft(TriTupleImpl<A, B, C> leftTuple) {
        return mappingABC.apply(leftTuple.factA, leftTuple.factB, leftTuple.factC);
    }

    @Override
    protected void updateOutTupleLeft(QuadTupleImpl<A, B, C, D> outTuple, TriTupleImpl<A, B, C> leftTuple) {
        outTuple.factA = leftTuple.factA;
        outTuple.factB = leftTuple.factB;
        outTuple.factC = leftTuple.factC;
    }

    @Override
    protected void updateOutTupleRight(QuadTupleImpl<A, B, C, D> outTuple, UniTupleImpl<D> rightTuple) {
        outTuple.factD = rightTuple.factA;
    }

    @Override
    protected QuadTupleImpl<A, B, C, D> createOutTuple(TriTupleImpl<A, B, C> leftTuple, UniTupleImpl<D> rightTuple) {
        return new QuadTupleImpl<>(leftTuple.factA, leftTuple.factB, leftTuple.factC, rightTuple.factA, outputStoreSize);
    }

    @Override
    public String toString() {
        return "JoinQuadNode";
    }

}
