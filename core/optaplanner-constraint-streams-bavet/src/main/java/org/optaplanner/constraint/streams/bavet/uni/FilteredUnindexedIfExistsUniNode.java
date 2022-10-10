package org.optaplanner.constraint.streams.bavet.uni;

import java.util.function.BiPredicate;

import org.optaplanner.constraint.streams.bavet.common.AbstractFilteredUnindexedIfExistsNode;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;

final class FilteredUnindexedIfExistsUniNode<A, B> extends AbstractFilteredUnindexedIfExistsNode<UniTuple<A>, B> {

    private final BiPredicate<A, B> filtering;

    public FilteredUnindexedIfExistsUniNode(boolean shouldExist, int inputStoreIndexLeftCounterEntry,
            int inputStoreIndexLeftTrackerList, int inputStoreIndexRightEntry, int inputStoreIndexRightTrackerList,
            TupleLifecycle<UniTuple<A>> nextNodesTupleLifecycle, BiPredicate<A, B> filtering) {
        super(shouldExist, inputStoreIndexLeftCounterEntry, inputStoreIndexLeftTrackerList, inputStoreIndexRightEntry,
                inputStoreIndexRightTrackerList, nextNodesTupleLifecycle);
        this.filtering = filtering;
    }

    @Override
    protected boolean testFiltering(UniTuple<A> leftTuple, UniTuple<B> rightTuple) {
        return filtering.test(leftTuple.getFactA(), rightTuple.getFactA());
    }

}
