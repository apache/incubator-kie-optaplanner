package org.optaplanner.constraint.streams.bavet.bi;

import org.optaplanner.constraint.streams.bavet.common.AbstractFilteredUnindexedIfExistsNode;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.uni.UniTuple;
import org.optaplanner.core.api.function.TriPredicate;

final class FilteredUnindexedIfExistsBiNode<A, B, C> extends AbstractFilteredUnindexedIfExistsNode<BiTuple<A, B>, C> {

    private final TriPredicate<A, B, C> filtering;

    public FilteredUnindexedIfExistsBiNode(boolean shouldExist, int inputStoreIndexLeftCounterEntry,
            int inputStoreIndexLeftTrackerList, int inputStoreIndexRightEntry, int inputStoreIndexRightTrackerList,
            TupleLifecycle<BiTuple<A, B>> nextNodesTupleLifecycle, TriPredicate<A, B, C> filtering) {
        super(shouldExist, inputStoreIndexLeftCounterEntry, inputStoreIndexLeftTrackerList, inputStoreIndexRightEntry,
                inputStoreIndexRightTrackerList, nextNodesTupleLifecycle);
        this.filtering = filtering;
    }

    @Override
    protected boolean testFiltering(BiTuple<A, B> leftTuple, UniTuple<C> rightTuple) {
        return filtering.test(leftTuple.getFactA(), leftTuple.getFactB(), rightTuple.getFactA());
    }

}
