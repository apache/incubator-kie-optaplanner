package org.optaplanner.constraint.streams.bavet.tri;

import org.optaplanner.constraint.streams.bavet.common.AbstractUnindexedIfExistsNode;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;

final class UnindexedIfExistsTriNode<A, B, C, D> extends AbstractUnindexedIfExistsNode<TriTuple<A, B, C>, D> {

    public UnindexedIfExistsTriNode(boolean shouldExist, int inputStoreIndexLeftCounterEntry, int inputStoreIndexRightEntry,
            TupleLifecycle<TriTuple<A, B, C>> nextNodesTupleLifecycle) {
        super(shouldExist, inputStoreIndexLeftCounterEntry, -1, inputStoreIndexRightEntry, -1, nextNodesTupleLifecycle);
    }

}
