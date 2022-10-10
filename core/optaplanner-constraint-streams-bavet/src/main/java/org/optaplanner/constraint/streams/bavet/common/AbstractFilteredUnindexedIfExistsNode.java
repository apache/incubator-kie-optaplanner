package org.optaplanner.constraint.streams.bavet.common;

import org.optaplanner.constraint.streams.bavet.common.collection.TupleList;
import org.optaplanner.constraint.streams.bavet.uni.UniTuple;

/**
 * There is a strong likelihood that any change made to this class
 * should also be made to {@link AbstractFilteredIndexedIfExistsNode}.
 *
 * @param <LeftTuple_>
 * @param <Right_>
 */
public abstract class AbstractFilteredUnindexedIfExistsNode<LeftTuple_ extends Tuple, Right_>
        extends AbstractUnindexedIfExistsNode<LeftTuple_, Right_> {

    protected AbstractFilteredUnindexedIfExistsNode(boolean shouldExist, int inputStoreIndexLeftCounterEntry,
            int inputStoreIndexLeftTrackerList, int inputStoreIndexRightEntry, int inputStoreIndexRightTrackerList,
            TupleLifecycle<LeftTuple_> nextNodesTupleLifecycle) {
        super(shouldExist, inputStoreIndexLeftCounterEntry, inputStoreIndexLeftTrackerList, inputStoreIndexRightEntry,
                inputStoreIndexRightTrackerList, nextNodesTupleLifecycle);
    }

    @Override
    protected void insertLeftMaybeFiltering(ExistsCounter<LeftTuple_> counter, LeftTuple_ leftTuple) {
        TupleList<ExistsFilteringTracker<LeftTuple_>> leftTrackerList = new TupleList<>();
        rightTupleList.forEach(rightTuple -> {
            if (testFiltering(leftTuple, rightTuple)) {
                counter.countRight++;
                TupleList<ExistsFilteringTracker<LeftTuple_>> rightTrackerList =
                        rightTuple.getStore(inputStoreIndexRightTrackerList);
                new ExistsFilteringTracker<>(counter, leftTrackerList, rightTrackerList);
            }
        });
        leftTuple.setStore(inputStoreIndexLeftTrackerList, leftTrackerList);
    }

    @Override
    protected void updateLeftMaybeFiltering(ExistsCounter<LeftTuple_> counter, LeftTuple_ leftTuple) {
        // Call filtering for the leftTuple and rightTuple combinations again
        TupleList<ExistsFilteringTracker<LeftTuple_>> leftTrackerList = leftTuple.getStore(inputStoreIndexLeftTrackerList);
        leftTrackerList.forEach(ExistsFilteringTracker::remove);
        counter.countRight = 0;
        rightTupleList.forEach(rightTuple -> {
            if (testFiltering(leftTuple, rightTuple)) {
                counter.countRight++;
                TupleList<ExistsFilteringTracker<LeftTuple_>> rightTrackerList =
                        rightTuple.getStore(inputStoreIndexRightTrackerList);
                new ExistsFilteringTracker<>(counter, leftTrackerList, rightTrackerList);
            }
        });
        updateCounterLeft(counter);
    }

    @Override
    protected void retractLeftMaybeFiltering(ExistsCounter<LeftTuple_> counter, LeftTuple_ leftTuple) {
        TupleList<ExistsFilteringTracker<LeftTuple_>> leftTrackerList = leftTuple.getStore(inputStoreIndexLeftTrackerList);
        leftTrackerList.forEach(ExistsFilteringTracker::remove);
    }

    @Override
    protected void insertRightMaybeFiltering(UniTuple<Right_> rightTuple) {
        TupleList<ExistsFilteringTracker<LeftTuple_>> rightTrackerList = new TupleList<>();
        leftCounterList.forEach(counter -> {
            if (testFiltering(counter.leftTuple, rightTuple)) {
                incrementCounterRight(counter);
                TupleList<ExistsFilteringTracker<LeftTuple_>> leftTrackerList =
                        counter.leftTuple.getStore(inputStoreIndexLeftTrackerList);
                new ExistsFilteringTracker<>(counter, leftTrackerList, rightTrackerList);
            }
        });
        rightTuple.setStore(inputStoreIndexRightTrackerList, rightTrackerList);
    }

    @Override
    protected void updateRightMaybeFiltering(UniTuple<Right_> rightTuple) {
        TupleList<ExistsFilteringTracker<LeftTuple_>> rightTrackerList = rightTuple.getStore(inputStoreIndexRightTrackerList);
        rightTrackerList.forEach(filteringTacker -> {
            decrementCounterRight(filteringTacker.counter);
            filteringTacker.remove();
        });
        leftCounterList.forEach(counter -> {
            if (testFiltering(counter.leftTuple, rightTuple)) {
                incrementCounterRight(counter);
                TupleList<ExistsFilteringTracker<LeftTuple_>> leftTrackerList =
                        counter.leftTuple.getStore(inputStoreIndexLeftTrackerList);
                new ExistsFilteringTracker<>(counter, leftTrackerList, rightTrackerList);
            }
        });
    }

    @Override
    protected void retractRightMaybeFiltering(UniTuple<Right_> rightTuple) {
        TupleList<ExistsFilteringTracker<LeftTuple_>> rightTrackerList = rightTuple.getStore(inputStoreIndexRightTrackerList);
        rightTrackerList.forEach(filteringTacker -> {
            decrementCounterRight(filteringTacker.counter);
            filteringTacker.remove();
        });
    }

    protected abstract boolean testFiltering(LeftTuple_ leftTuple, UniTuple<Right_> rightTuple);

}
