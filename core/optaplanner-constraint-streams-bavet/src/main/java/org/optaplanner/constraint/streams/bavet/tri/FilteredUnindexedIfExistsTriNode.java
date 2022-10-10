package org.optaplanner.constraint.streams.bavet.tri;

import org.optaplanner.constraint.streams.bavet.common.AbstractFilteredUnindexedIfExistsNode;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.uni.UniTuple;
import org.optaplanner.core.api.function.QuadPredicate;

final class FilteredUnindexedIfExistsTriNode<A, B, C, D> extends AbstractFilteredUnindexedIfExistsNode<TriTuple<A, B, C>, D> {

    private final QuadPredicate<A, B, C, D> filtering;

    public FilteredUnindexedIfExistsTriNode(boolean shouldExist, int inputStoreIndexLeftCounterEntry,
            int inputStoreIndexLeftTrackerList, int inputStoreIndexRightEntry, int inputStoreIndexRightTrackerList,
            TupleLifecycle<TriTuple<A, B, C>> nextNodesTupleLifecycle, QuadPredicate<A, B, C, D> filtering) {
        super(shouldExist, inputStoreIndexLeftCounterEntry, inputStoreIndexLeftTrackerList, inputStoreIndexRightEntry,
                inputStoreIndexRightTrackerList, nextNodesTupleLifecycle);
        this.filtering = filtering;
    }

    @Override
    protected boolean testFiltering(TriTuple<A, B, C> leftTuple, UniTuple<D> rightTuple) {
        return filtering.test(leftTuple.getFactA(), leftTuple.getFactB(), leftTuple.getFactC(), rightTuple.getFactA());
    }

}
