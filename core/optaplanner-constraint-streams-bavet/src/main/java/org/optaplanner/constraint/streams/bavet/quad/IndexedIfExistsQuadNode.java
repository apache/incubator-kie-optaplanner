package org.optaplanner.constraint.streams.bavet.quad;

import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.AbstractIndexedIfExistsNode;
import org.optaplanner.constraint.streams.bavet.common.ExistsCounter;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.index.IndexProperties;
import org.optaplanner.constraint.streams.bavet.common.index.Indexer;
import org.optaplanner.constraint.streams.bavet.uni.UniTuple;
import org.optaplanner.core.api.function.QuadFunction;

final class IndexedIfExistsQuadNode<A, B, C, D, E> extends AbstractIndexedIfExistsNode<QuadTuple<A, B, C, D>, E> {

    private final QuadFunction<A, B, C, D, IndexProperties> mappingABCD;

    public IndexedIfExistsQuadNode(boolean shouldExist, QuadFunction<A, B, C, D, IndexProperties> mappingABCD,
            Function<E, IndexProperties> mappingE, int inputStoreIndexLeftProperties, int inputStoreIndexLeftCounterEntry,
            int inputStoreIndexRightProperties, int inputStoreIndexRightEntry,
            TupleLifecycle<QuadTuple<A, B, C, D>> nextNodesTupleLifecycle,
            Indexer<ExistsCounter<QuadTuple<A, B, C, D>>> indexerABCD, Indexer<UniTuple<E>> indexerE) {
        super(shouldExist, mappingE, inputStoreIndexLeftProperties, inputStoreIndexLeftCounterEntry, -1,
                inputStoreIndexRightProperties, inputStoreIndexRightEntry, -1, nextNodesTupleLifecycle, indexerABCD, indexerE);
        this.mappingABCD = mappingABCD;
    }

    @Override
    protected IndexProperties createIndexProperties(QuadTuple<A, B, C, D> leftTuple) {
        return mappingABCD.apply(leftTuple.getFactA(), leftTuple.getFactB(), leftTuple.getFactC(), leftTuple.getFactD());
    }

}
