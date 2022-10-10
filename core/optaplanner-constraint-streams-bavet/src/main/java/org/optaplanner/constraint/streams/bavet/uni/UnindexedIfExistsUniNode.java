package org.optaplanner.constraint.streams.bavet.uni;

import org.optaplanner.constraint.streams.bavet.common.AbstractUnindexedIfExistsNode;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;

final class UnindexedIfExistsUniNode<A, B> extends AbstractUnindexedIfExistsNode<UniTuple<A>, B> {

    public UnindexedIfExistsUniNode(boolean shouldExist, int inputStoreIndexLeftCounterEntry, int inputStoreIndexRightEntry,
            TupleLifecycle<UniTuple<A>> nextNodesTupleLifecycle) {
        super(shouldExist, inputStoreIndexLeftCounterEntry, -1, inputStoreIndexRightEntry, -1, nextNodesTupleLifecycle);
    }

}
