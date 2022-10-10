package org.optaplanner.constraint.streams.bavet.bi;

import org.optaplanner.constraint.streams.bavet.common.AbstractUnindexedIfExistsNode;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;

final class UnindexedIfExistsBiNode<A, B, C> extends AbstractUnindexedIfExistsNode<BiTuple<A, B>, C> {

    public UnindexedIfExistsBiNode(boolean shouldExist, int inputStoreIndexLeftCounterEntry, int inputStoreIndexRightEntry,
            TupleLifecycle<BiTuple<A, B>> nextNodesTupleLifecycle) {
        super(shouldExist, inputStoreIndexLeftCounterEntry, -1, inputStoreIndexRightEntry, -1, nextNodesTupleLifecycle);
    }

}
