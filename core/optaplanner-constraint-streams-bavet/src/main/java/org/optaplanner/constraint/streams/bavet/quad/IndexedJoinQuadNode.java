package org.optaplanner.constraint.streams.bavet.quad;

import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.AbstractIndexedJoinNode;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.index.IndexProperties;
import org.optaplanner.constraint.streams.bavet.common.index.Indexer;
import org.optaplanner.constraint.streams.bavet.common.tuple.QuadTuple;
import org.optaplanner.constraint.streams.bavet.common.tuple.QuadTupleImpl;
import org.optaplanner.constraint.streams.bavet.common.tuple.TriTuple;
import org.optaplanner.constraint.streams.bavet.common.tuple.UniTuple;
import org.optaplanner.core.api.function.QuadPredicate;
import org.optaplanner.core.api.function.TriFunction;

final class IndexedJoinQuadNode<A, B, C, D>
        extends AbstractIndexedJoinNode<TriTuple<A, B, C>, D, QuadTuple<A, B, C, D>, QuadTupleImpl<A, B, C, D>> {

    private final TriFunction<A, B, C, IndexProperties> mappingABC;
    private final QuadPredicate<A, B, C, D> filtering;
    private final int outputStoreSize;

    public IndexedJoinQuadNode(TriFunction<A, B, C, IndexProperties> mappingABC, Function<D, IndexProperties> mappingD,
            int inputStoreIndexABC, int inputStoreIndexEntryABC, int inputStoreIndexOutTupleListABC,
            int inputStoreIndexD, int inputStoreIndexEntryD, int inputStoreIndexOutTupleListD,
            TupleLifecycle<QuadTuple<A, B, C, D>> nextNodesTupleLifecycle, QuadPredicate<A, B, C, D> filtering,
            int outputStoreSize,
            int outputStoreIndexOutEntryABC, int outputStoreIndexOutEntryD,
            Indexer<TriTuple<A, B, C>> indexerABC,
            Indexer<UniTuple<D>> indexerD) {
        super(mappingD,
                inputStoreIndexABC, inputStoreIndexEntryABC, inputStoreIndexOutTupleListABC,
                inputStoreIndexD, inputStoreIndexEntryD, inputStoreIndexOutTupleListD,
                nextNodesTupleLifecycle, filtering != null,
                outputStoreIndexOutEntryABC, outputStoreIndexOutEntryD,
                indexerABC, indexerD);
        this.mappingABC = mappingABC;
        this.filtering = filtering;
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected IndexProperties createIndexPropertiesLeft(TriTuple<A, B, C> leftTuple) {
        return mappingABC.apply(leftTuple.getA(), leftTuple.getB(), leftTuple.getC());
    }

    @Override
    protected QuadTupleImpl<A, B, C, D> createOutTuple(TriTuple<A, B, C> leftTuple, UniTuple<D> rightTuple) {
        return new QuadTupleImpl<>(leftTuple.getA(), leftTuple.getB(), leftTuple.getC(), rightTuple.getA(),
                outputStoreSize);
    }

    @Override
    protected void setOutTupleLeftFacts(QuadTupleImpl<A, B, C, D> outTuple, TriTuple<A, B, C> leftTuple) {
        outTuple.setA(leftTuple.getA());
        outTuple.setB(leftTuple.getB());
        outTuple.setC(leftTuple.getC());
    }

    @Override
    protected void setOutTupleRightFact(QuadTupleImpl<A, B, C, D> outTuple, UniTuple<D> rightTuple) {
        outTuple.setD(rightTuple.getA());
    }

    @Override
    protected boolean testFiltering(TriTuple<A, B, C> leftTuple, UniTuple<D> rightTuple) {
        return filtering.test(leftTuple.getA(), leftTuple.getB(), leftTuple.getC(), rightTuple.getA());
    }

}
