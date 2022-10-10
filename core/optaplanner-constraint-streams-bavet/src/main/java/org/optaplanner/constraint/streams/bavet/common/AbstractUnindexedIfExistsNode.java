package org.optaplanner.constraint.streams.bavet.common;

import org.optaplanner.constraint.streams.bavet.common.collection.TupleList;
import org.optaplanner.constraint.streams.bavet.common.collection.TupleListEntry;
import org.optaplanner.constraint.streams.bavet.uni.UniTuple;

/**
 * There is a strong likelihood that any change made to this class
 * should also be made to {@link AbstractIndexedIfExistsNode}.
 * <p>
 * This class also has a child ({@link AbstractFilteredUnindexedIfExistsNode}) which adds logic regarding filtering.
 * It is possible that changes to this class will also require changes to the child.
 *
 * @param <LeftTuple_>
 * @param <Right_>
 */
public abstract class AbstractUnindexedIfExistsNode<LeftTuple_ extends Tuple, Right_>
        extends AbstractIfExistsNode<LeftTuple_, Right_> {

    protected final int inputStoreIndexLeftCounterEntry;

    protected final int inputStoreIndexRightEntry;

    // Acts as a leftTupleList too
    protected final TupleList<ExistsCounter<LeftTuple_>> leftCounterList = new TupleList<>();
    protected final TupleList<UniTuple<Right_>> rightTupleList = new TupleList<>();

    protected AbstractUnindexedIfExistsNode(boolean shouldExist, int inputStoreIndexLeftCounterEntry,
            int inputStoreIndexLeftTrackerList, int inputStoreIndexRightEntry, int inputStoreIndexRightTrackerList,
            TupleLifecycle<LeftTuple_> nextNodesTupleLifecycle) {
        super(shouldExist, inputStoreIndexLeftTrackerList, inputStoreIndexRightTrackerList, nextNodesTupleLifecycle);
        this.inputStoreIndexLeftCounterEntry = inputStoreIndexLeftCounterEntry;
        this.inputStoreIndexRightEntry = inputStoreIndexRightEntry;
    }

    @Override
    public final void insertLeft(LeftTuple_ leftTuple) {
        if (leftTuple.getStore(inputStoreIndexLeftCounterEntry) != null) {
            throw new IllegalStateException("Impossible state: the input for the tuple (" + leftTuple
                    + ") was already added in the tupleStore.");
        }
        ExistsCounter<LeftTuple_> counter = new ExistsCounter<>(leftTuple);
        TupleListEntry<ExistsCounter<LeftTuple_>> counterEntry = leftCounterList.add(counter);
        leftTuple.setStore(inputStoreIndexLeftCounterEntry, counterEntry);

        insertLeftMaybeFiltering(counter, leftTuple);
        initCounterLeft(counter);
    }

    protected void insertLeftMaybeFiltering(ExistsCounter<LeftTuple_> counter, LeftTuple_ leftTuple) {
        counter.countRight = rightTupleList.size();
    }

    @Override
    public final void updateLeft(LeftTuple_ leftTuple) {
        TupleListEntry<ExistsCounter<LeftTuple_>> counterEntry = leftTuple.getStore(inputStoreIndexLeftCounterEntry);
        if (counterEntry == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            insertLeft(leftTuple);
            return;
        }
        ExistsCounter<LeftTuple_> counter = counterEntry.getElement();
        // The indexers contain counters in the DEAD state, to track the rightCount.
        updateLeftMaybeFiltering(counter, leftTuple);
    }

    protected void updateLeftMaybeFiltering(ExistsCounter<LeftTuple_> counter, LeftTuple_ leftTuple) {
        updateUnchangedCounterLeft(counter);
    }

    @Override
    public final void retractLeft(LeftTuple_ leftTuple) {
        TupleListEntry<ExistsCounter<LeftTuple_>> counterEntry = leftTuple.removeStore(inputStoreIndexLeftCounterEntry);
        if (counterEntry == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            return;
        }
        ExistsCounter<LeftTuple_> counter = counterEntry.getElement();
        counterEntry.remove();
        retractLeftMaybeFiltering(counter, leftTuple);
        killCounterLeft(counter);
    }

    protected void retractLeftMaybeFiltering(ExistsCounter<LeftTuple_> counter, LeftTuple_ leftTuple) {
        // Intentionally empty.
    }

    @Override
    public final void insertRight(UniTuple<Right_> rightTuple) {
        if (rightTuple.getStore(inputStoreIndexRightEntry) != null) {
            throw new IllegalStateException("Impossible state: the input for the tuple (" + rightTuple
                    + ") was already added in the tupleStore.");
        }
        TupleListEntry<UniTuple<Right_>> rightEntry = rightTupleList.add(rightTuple);
        rightTuple.setStore(inputStoreIndexRightEntry, rightEntry);
        insertRightMaybeFiltering(rightTuple);
    }

    protected void insertRightMaybeFiltering(UniTuple<Right_> rightTuple) {
        leftCounterList.forEach(this::incrementCounterRight);
    }

    @Override
    public final void updateRight(UniTuple<Right_> rightTuple) {
        TupleListEntry<UniTuple<Right_>> rightEntry = rightTuple.getStore(inputStoreIndexRightEntry);
        if (rightEntry == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            insertRight(rightTuple);
            return;
        }
        updateRightMaybeFiltering(rightTuple);
    }

    protected void updateRightMaybeFiltering(UniTuple<Right_> rightTuple) {
        // Intentionally empty.
    }

    @Override
    public final void retractRight(UniTuple<Right_> rightTuple) {
        TupleListEntry<UniTuple<Right_>> rightEntry = rightTuple.removeStore(inputStoreIndexRightEntry);
        if (rightEntry == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            return;
        }
        rightEntry.remove();
        retractRightMaybeFiltering(rightTuple);
    }

    protected void retractRightMaybeFiltering(UniTuple<Right_> rightTuple) {
        leftCounterList.forEach(this::decrementCounterRight);
    }

}
