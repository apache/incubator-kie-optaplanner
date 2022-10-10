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
        track(counter, leftTuple, leftTrackerList);
        leftTuple.setStore(inputStoreIndexLeftTrackerList, leftTrackerList);
    }

    private void track(ExistsCounter<LeftTuple_> counter, LeftTuple_ leftTuple,
            TupleList<ExistsFilteringTracker<LeftTuple_>> leftTrackerList) {
        for (UniTuple<Right_> rightTuple : rightTupleList) {
            if (testFiltering(leftTuple, rightTuple)) {
                counter.countRight++;
                TupleList<ExistsFilteringTracker<LeftTuple_>> rightTrackerList =
                        rightTuple.getStore(inputStoreIndexRightTrackerList);
                new ExistsFilteringTracker<>(counter, leftTrackerList, rightTrackerList);
            }
        }
    }

    @Override
    protected void updateLeftMaybeFiltering(ExistsCounter<LeftTuple_> counter, LeftTuple_ leftTuple) {
        // Call filtering for the leftTuple and rightTuple combinations again
        TupleList<ExistsFilteringTracker<LeftTuple_>> leftTrackerList = leftTuple.getStore(inputStoreIndexLeftTrackerList);
        leftTrackerList.forEach(ExistsFilteringTracker::remove);
        counter.countRight = 0;
        track(counter, leftTuple, leftTrackerList);
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
        track(rightTuple, rightTrackerList);
        rightTuple.setStore(inputStoreIndexRightTrackerList, rightTrackerList);
    }

    private void track(UniTuple<Right_> rightTuple, TupleList<ExistsFilteringTracker<LeftTuple_>> rightTrackerList) {
        for (ExistsCounter<LeftTuple_> counter : leftCounterList) {
            if (testFiltering(counter.leftTuple, rightTuple)) {
                incrementCounterRight(counter);
                TupleList<ExistsFilteringTracker<LeftTuple_>> leftTrackerList =
                        counter.leftTuple.getStore(inputStoreIndexLeftTrackerList);
                new ExistsFilteringTracker<>(counter, leftTrackerList, rightTrackerList);
            }
        }
    }

    @Override
    protected void updateRightMaybeFiltering(UniTuple<Right_> rightTuple) {
        TupleList<ExistsFilteringTracker<LeftTuple_>> rightTrackerList = rightTuple.getStore(inputStoreIndexRightTrackerList);
        for (ExistsFilteringTracker<LeftTuple_> tracker : rightTrackerList) {
            decrementCounterRight(tracker.counter);
            tracker.remove();
        }
        track(rightTuple, rightTrackerList);
    }

    @Override
    protected void retractRightMaybeFiltering(UniTuple<Right_> rightTuple) {
        TupleList<ExistsFilteringTracker<LeftTuple_>> rightTrackerList = rightTuple.getStore(inputStoreIndexRightTrackerList);
        for (ExistsFilteringTracker<LeftTuple_> tracker : rightTrackerList) {
            decrementCounterRight(tracker.counter);
            tracker.remove();
        }
    }

    protected abstract boolean testFiltering(LeftTuple_ leftTuple, UniTuple<Right_> rightTuple);

}
