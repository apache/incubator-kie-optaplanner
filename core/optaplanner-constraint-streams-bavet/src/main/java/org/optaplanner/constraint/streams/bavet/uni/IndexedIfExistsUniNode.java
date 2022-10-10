package org.optaplanner.constraint.streams.bavet.uni;

import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.AbstractIndexedIfExistsNode;
import org.optaplanner.constraint.streams.bavet.common.ExistsCounter;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.index.IndexProperties;
import org.optaplanner.constraint.streams.bavet.common.index.Indexer;

final class IndexedIfExistsUniNode<A, B> extends AbstractIndexedIfExistsNode<UniTuple<A>, B> {

    private final Function<A, IndexProperties> mappingA;

    public IndexedIfExistsUniNode(boolean shouldExist, Function<A, IndexProperties> mappingA,
            Function<B, IndexProperties> mappingB, int inputStoreIndexLeftProperties, int inputStoreIndexLeftCounterEntry,
            int inputStoreIndexRightProperties, int inputStoreIndexRightEntry,
            TupleLifecycle<UniTuple<A>> nextNodesTupleLifecycle, Indexer<ExistsCounter<UniTuple<A>>> indexerA,
            Indexer<UniTuple<B>> indexerB) {
        super(shouldExist, mappingB, inputStoreIndexLeftProperties, inputStoreIndexLeftCounterEntry, -1,
                inputStoreIndexRightProperties, inputStoreIndexRightEntry, -1, nextNodesTupleLifecycle, indexerA, indexerB);
        this.mappingA = mappingA;
    }

    @Override
    protected IndexProperties createIndexProperties(UniTuple<A> leftTuple) {
        return mappingA.apply(leftTuple.getFactA());
    }

}
