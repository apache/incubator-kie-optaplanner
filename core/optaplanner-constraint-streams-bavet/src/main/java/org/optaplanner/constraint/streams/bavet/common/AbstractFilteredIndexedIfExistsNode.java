package org.optaplanner.constraint.streams.bavet.common;

import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.collection.TupleList;
import org.optaplanner.constraint.streams.bavet.common.collection.TupleListEntry;
import org.optaplanner.constraint.streams.bavet.common.index.IndexProperties;
import org.optaplanner.constraint.streams.bavet.common.index.Indexer;
import org.optaplanner.constraint.streams.bavet.uni.UniTuple;

/**
 * There is a strong likelihood that any change to this class, which is not related to indexing,
 * should also be made to {@link AbstractFilteredUnindexedIfExistsNode}.
 *
 * @param <LeftTuple_>
 * @param <Right_>
 */
public abstract class AbstractFilteredIndexedIfExistsNode<LeftTuple_ extends Tuple, Right_>
        extends AbstractIndexedIfExistsNode<LeftTuple_, Right_> {

    protected AbstractFilteredIndexedIfExistsNode(boolean shouldExist, Function<Right_, IndexProperties> mappingRight,
            int inputStoreIndexLeftProperties, int inputStoreIndexLeftCounterEntry, int inputStoreIndexLeftTrackerList,
            int inputStoreIndexRightProperties, int inputStoreIndexRightEntry, int inputStoreIndexRightTrackerList,
            TupleLifecycle<LeftTuple_> nextNodesTupleLifecycle, Indexer<ExistsCounter<LeftTuple_>> indexerLeft,
            Indexer<UniTuple<Right_>> indexerRight) {
        super(shouldExist, mappingRight, inputStoreIndexLeftProperties, inputStoreIndexLeftCounterEntry,
                inputStoreIndexLeftTrackerList, inputStoreIndexRightProperties, inputStoreIndexRightEntry,
                inputStoreIndexRightTrackerList, nextNodesTupleLifecycle, indexerLeft, indexerRight);
    }

    @Override
    protected void insertLeftMaybeFiltering(ExistsCounter<LeftTuple_> counter, LeftTuple_ leftTuple,
            IndexProperties indexProperties) {
        TupleList<ExistsFilteringTracker<LeftTuple_>> leftTrackerList = new TupleList<>();
        indexerRight.forEach(indexProperties, rightTuple -> {
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
    protected void updateLeftWithoutReindex(ExistsCounter<LeftTuple_> counter, LeftTuple_ leftTuple,
            IndexProperties oldIndexProperties) {
        // Call filtering for the leftTuple and rightTuple combinations again
        TupleList<ExistsFilteringTracker<LeftTuple_>> leftTrackerList = leftTuple.getStore(inputStoreIndexLeftTrackerList);
        leftTrackerList.forEach(ExistsFilteringTracker::remove);
        counter.countRight = 0;
        indexerRight.forEach(oldIndexProperties, rightTuple -> {
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
    protected void updateLeftWithReindex(ExistsCounter<LeftTuple_> counter,
            TupleListEntry<ExistsCounter<LeftTuple_>> counterEntry, LeftTuple_ leftTuple,
            IndexProperties oldIndexProperties, IndexProperties newIndexProperties) {
        indexerLeft.remove(oldIndexProperties, counterEntry);
        TupleList<ExistsFilteringTracker> leftTrackerList = leftTuple.getStore(inputStoreIndexLeftTrackerList);
        leftTrackerList.forEach(ExistsFilteringTracker::remove);
        counter.countRight = 0;
        leftTuple.setStore(inputStoreIndexLeftProperties, newIndexProperties);
        counterEntry = indexerLeft.put(newIndexProperties, counter);
        leftTuple.setStore(inputStoreIndexLeftCounterEntry, counterEntry);
        TupleList<ExistsFilteringTracker<LeftTuple_>> newLeftTrackerList = new TupleList<>();
        indexerRight.forEach(newIndexProperties, rightTuple -> {
            if (testFiltering(leftTuple, rightTuple)) {
                counter.countRight++;
                TupleList<ExistsFilteringTracker<LeftTuple_>> rightTrackerList =
                        rightTuple.getStore(inputStoreIndexRightTrackerList);
                new ExistsFilteringTracker<>(counter, newLeftTrackerList, rightTrackerList);
            }
        });
        leftTuple.setStore(inputStoreIndexLeftTrackerList, newLeftTrackerList);
        updateCounterLeft(counter);
    }

    @Override
    protected void retractLeftMaybeFiltering(LeftTuple_ leftTuple) {
        TupleList<ExistsFilteringTracker<LeftTuple_>> leftTrackerList = leftTuple.getStore(inputStoreIndexLeftTrackerList);
        leftTrackerList.forEach(ExistsFilteringTracker::remove);
    }

    @Override
    protected void insertRightMaybeFiltering(UniTuple<Right_> rightTuple, IndexProperties indexProperties) {
        TupleList<ExistsFilteringTracker<LeftTuple_>> rightTrackerList = new TupleList<>();
        indexerLeft.forEach(indexProperties, counter -> {
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
    protected void updateRightWithoutReindex(UniTuple<Right_> rightTuple, IndexProperties oldIndexProperties) {
        TupleList<ExistsFilteringTracker<LeftTuple_>> rightTrackerList = rightTuple.getStore(inputStoreIndexRightTrackerList);
        rightTrackerList.forEach(filteringTacker -> {
            decrementCounterRight(filteringTacker.counter);
            filteringTacker.remove();
        });
        indexerLeft.forEach(oldIndexProperties, counter -> {
            if (testFiltering(counter.leftTuple, rightTuple)) {
                incrementCounterRight(counter);
                TupleList<ExistsFilteringTracker<LeftTuple_>> leftTrackerList =
                        counter.leftTuple.getStore(inputStoreIndexLeftTrackerList);
                new ExistsFilteringTracker<>(counter, leftTrackerList, rightTrackerList);
            }
        });
    }

    @Override
    protected void updateRightWithReindex(UniTuple<Right_> rightTuple, IndexProperties oldIndexProperties,
            IndexProperties newIndexProperties) {
        TupleListEntry<UniTuple<Right_>> rightEntry = rightTuple.getStore(inputStoreIndexRightEntry);
        indexerRight.remove(oldIndexProperties, rightEntry);
        TupleList<ExistsFilteringTracker<LeftTuple_>> rightTrackerList = rightTuple.getStore(inputStoreIndexRightTrackerList);
        rightTrackerList.forEach(filteringTacker -> {
            decrementCounterRight(filteringTacker.counter);
            filteringTacker.remove();
        });
        rightTuple.setStore(inputStoreIndexRightProperties, newIndexProperties);
        rightEntry = indexerRight.put(newIndexProperties, rightTuple);
        rightTuple.setStore(inputStoreIndexRightEntry, rightEntry);
        TupleList<ExistsFilteringTracker<LeftTuple_>> newRightTrackerList = new TupleList<>();
        indexerLeft.forEach(newIndexProperties, counter -> {
            if (testFiltering(counter.leftTuple, rightTuple)) {
                incrementCounterRight(counter);
                TupleList<ExistsFilteringTracker<LeftTuple_>> leftTrackerList =
                        counter.leftTuple.getStore(inputStoreIndexLeftTrackerList);
                new ExistsFilteringTracker<>(counter, leftTrackerList, newRightTrackerList);
            }
        });
        rightTuple.setStore(inputStoreIndexRightTrackerList, newRightTrackerList);
    }

    @Override
    protected void retractRightMaybeFiltering(UniTuple<Right_> rightTuple, IndexProperties indexProperties) {
        TupleList<ExistsFilteringTracker<LeftTuple_>> rightTrackerList = rightTuple.getStore(inputStoreIndexRightTrackerList);
        rightTrackerList.forEach(filteringTacker -> {
            decrementCounterRight(filteringTacker.counter);
            filteringTacker.remove();
        });
    }

}
