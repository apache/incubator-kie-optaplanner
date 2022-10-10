package org.optaplanner.constraint.streams.bavet.quad;

import org.optaplanner.constraint.streams.bavet.common.AbstractUnindexedIfExistsNode;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;

final class UnindexedIfExistsQuadNode<A, B, C, D, E> extends AbstractUnindexedIfExistsNode<QuadTuple<A, B, C, D>, E> {

    public UnindexedIfExistsQuadNode(boolean shouldExist, int inputStoreIndexLeftCounterEntry, int inputStoreIndexRightEntry,
            TupleLifecycle<QuadTuple<A, B, C, D>> nextNodesTupleLifecycle) {
        super(shouldExist, inputStoreIndexLeftCounterEntry, -1, inputStoreIndexRightEntry, -1, nextNodesTupleLifecycle);
    }

}
