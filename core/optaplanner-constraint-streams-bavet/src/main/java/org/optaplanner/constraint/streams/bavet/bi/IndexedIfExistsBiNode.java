package org.optaplanner.constraint.streams.bavet.bi;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.AbstractIndexedIfExistsNode;
import org.optaplanner.constraint.streams.bavet.common.ExistsCounter;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.index.IndexProperties;
import org.optaplanner.constraint.streams.bavet.common.index.Indexer;
import org.optaplanner.constraint.streams.bavet.uni.UniTuple;

final class IndexedIfExistsBiNode<A, B, C> extends AbstractIndexedIfExistsNode<BiTuple<A, B>, C> {

    private final BiFunction<A, B, IndexProperties> mappingAB;

    public IndexedIfExistsBiNode(boolean shouldExist, BiFunction<A, B, IndexProperties> mappingAB,
            Function<C, IndexProperties> mappingC, int inputStoreIndexLeftProperties, int inputStoreIndexLeftCounterEntry,
            int inputStoreIndexRightProperties, int inputStoreIndexRightEntry,
            TupleLifecycle<BiTuple<A, B>> nextNodesTupleLifecycle, Indexer<ExistsCounter<BiTuple<A, B>>> indexerAB,
            Indexer<UniTuple<C>> indexerC) {
        super(shouldExist, mappingC, inputStoreIndexLeftProperties, inputStoreIndexLeftCounterEntry, -1,
                inputStoreIndexRightProperties, inputStoreIndexRightEntry, -1, nextNodesTupleLifecycle, indexerAB, indexerC);
        this.mappingAB = mappingAB;
    }

    @Override
    protected IndexProperties createIndexProperties(BiTuple<A, B> leftTuple) {
        return mappingAB.apply(leftTuple.getFactA(), leftTuple.getFactB());
    }

}
