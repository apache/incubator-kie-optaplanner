package org.optaplanner.constraint.streams.bavet.common;

import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.collection.TupleList;
import org.optaplanner.constraint.streams.bavet.common.collection.TupleListEntry;
import org.optaplanner.constraint.streams.bavet.common.index.IndexProperties;
import org.optaplanner.constraint.streams.bavet.common.index.Indexer;
import org.optaplanner.constraint.streams.bavet.uni.UniTuple;

/**
 * There is a strong likelihood that any change to this class, which is not related to indexing,
 * should also be made to {@link AbstractUnindexedIfExistsNode}.
 *
 * @param <LeftTuple_>
 * @param <Right_>
 */
public abstract class AbstractIndexedIfExistsNode<LeftTuple_ extends Tuple, Right_>
        extends AbstractIfExistsNode<LeftTuple_, Right_>
        implements LeftTupleLifecycle<LeftTuple_>, RightTupleLifecycle<UniTuple<Right_>> {

    private final Function<Right_, IndexProperties> mappingRight;
    private final int inputStoreIndexLeftProperties;
    private final int inputStoreIndexLeftCounterEntry;
    private final int inputStoreIndexRightProperties;
    private final int inputStoreIndexRightEntry;
    private final Indexer<ExistsCounter<LeftTuple_>> indexerLeft;
    private final Indexer<UniTuple<Right_>> indexerRight;

    protected AbstractIndexedIfExistsNode(boolean shouldExist,
            Function<Right_, IndexProperties> mappingRight,
            int inputStoreIndexLeftProperties, int inputStoreIndexLeftCounterEntry, int inputStoreIndexLeftTrackerList,
            int inputStoreIndexRightProperties, int inputStoreIndexRightEntry, int inputStoreIndexRightTrackerList,
            TupleLifecycle<LeftTuple_> nextNodesTupleLifecycle,
            Indexer<ExistsCounter<LeftTuple_>> indexerLeft,
            Indexer<UniTuple<Right_>> indexerRight,
            boolean isFiltering) {
        super(shouldExist, inputStoreIndexLeftTrackerList, inputStoreIndexRightTrackerList,
                nextNodesTupleLifecycle, isFiltering);
        this.mappingRight = mappingRight;
        this.inputStoreIndexLeftProperties = inputStoreIndexLeftProperties;
        this.inputStoreIndexLeftCounterEntry = inputStoreIndexLeftCounterEntry;
        this.inputStoreIndexRightProperties = inputStoreIndexRightProperties;
        this.inputStoreIndexRightEntry = inputStoreIndexRightEntry;
        this.indexerLeft = indexerLeft;
        this.indexerRight = indexerRight;
    }

    @Override
    public final void insertLeft(LeftTuple_ leftTuple) {
        if (leftTuple.getStore(inputStoreIndexLeftProperties) != null) {
            throw new IllegalStateException("Impossible state: the input for the tuple (" + leftTuple
                    + ") was already added in the tupleStore.");
        }
        IndexProperties indexProperties = createIndexProperties(leftTuple);
        leftTuple.setStore(inputStoreIndexLeftProperties, indexProperties);

        ExistsCounter<LeftTuple_> counter = new ExistsCounter<>(leftTuple, shouldExist);
        TupleListEntry<ExistsCounter<LeftTuple_>> counterEntry = indexerLeft.put(indexProperties, counter);
        leftTuple.setStore(inputStoreIndexLeftCounterEntry, counterEntry);

        if (!isFiltering) {
            counter.countRight = indexerRight.size(indexProperties);
        } else {
            TupleList<FilteringTracker> leftTrackerList = new TupleList<>();
            indexerRight.visit(indexProperties, rightEntry -> {
                UniTuple<Right_> rightTuple = rightEntry.getElement();
                if (testFiltering(leftTuple, rightTuple)) {
                    counter.countRight++;
                    TupleList<FilteringTracker> rightTrackerList = rightTuple.getStore(inputStoreIndexRightTrackerList);
                    new FilteringTracker(counter, rightTuple, leftTrackerList, rightTrackerList);
                }
            });
            leftTuple.setStore(inputStoreIndexLeftTrackerList, leftTrackerList);
        }
        if (counter.isAlive()) {
            counter.state = BavetTupleState.CREATING;
            dirtyCounterQueue.add(counter);
        }
    }

    @Override
    public final void updateLeft(LeftTuple_ leftTuple) {
        IndexProperties oldIndexProperties = leftTuple.getStore(inputStoreIndexLeftProperties);
        if (oldIndexProperties == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            insertLeft(leftTuple);
            return;
        }
        IndexProperties newIndexProperties = createIndexProperties(leftTuple);
        TupleListEntry<ExistsCounter<LeftTuple_>> counterEntry = leftTuple.getStore(inputStoreIndexLeftCounterEntry);
        ExistsCounter<LeftTuple_> counter = counterEntry.getElement();

        if (oldIndexProperties.equals(newIndexProperties)) {
            // No need for re-indexing because the index properties didn't change
            // The indexers contain counters in the DEAD state, to track the rightCount.
            if (!isFiltering) {
                processCounterUpdate(counter);
            } else {
                // Call filtering for the leftTuple and rightTuple combinations again
                TupleList<FilteringTracker> leftTrackerList = leftTuple.getStore(inputStoreIndexLeftTrackerList);
                leftTrackerList.forEach(FilteringTracker::remove);
                counter.countRight = 0;
                indexerRight.visit(oldIndexProperties, rightEntry -> {
                    UniTuple<Right_> rightTuple = rightEntry.getElement();
                    if (testFiltering(leftTuple, rightTuple)) {
                        counter.countRight++;
                        TupleList<FilteringTracker> rightTrackerList = rightTuple.getStore(inputStoreIndexRightTrackerList);
                        new FilteringTracker(counter, rightTuple, leftTrackerList, rightTrackerList);
                    }
                });
            }
        } else {
            indexerLeft.remove(oldIndexProperties, counterEntry);
            if (isFiltering) {
                TupleList<FilteringTracker> leftTrackerList = leftTuple.getStore(inputStoreIndexLeftTrackerList);
                leftTrackerList.forEach(FilteringTracker::remove);
            }
            leftTuple.setStore(inputStoreIndexLeftProperties, newIndexProperties);
            counterEntry = indexerLeft.put(newIndexProperties, counter);
            leftTuple.setStore(inputStoreIndexLeftCounterEntry, counterEntry);
            if (!isFiltering) {
                counter.countRight = indexerRight.size(newIndexProperties);
            } else {
                TupleList<FilteringTracker> leftTrackerList = new TupleList<>();
                indexerRight.visit(newIndexProperties, rightEntry -> {
                    UniTuple<Right_> rightTuple = rightEntry.getElement();
                    if (testFiltering(leftTuple, rightTuple)) {
                        counter.countRight++;
                        TupleList<FilteringTracker> rightTrackerList = rightTuple.getStore(inputStoreIndexRightTrackerList);
                        new FilteringTracker(counter, rightTuple, leftTrackerList, rightTrackerList);
                    }
                });
                leftTuple.setStore(inputStoreIndexLeftTrackerList, leftTrackerList);
            }
        }
        if (counter.isAlive()) {
            // Insert or update
            insertOrUpdateCounter(counter);
        } else {
            // Retract or remain dead
            retractOrRemainDeadCounter(counter);
        }
    }

    @Override
    public final void retractLeft(LeftTuple_ leftTuple) {
        IndexProperties indexProperties = leftTuple.getStore(inputStoreIndexLeftProperties);
        if (indexProperties == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            return;
        }
        leftTuple.setStore(inputStoreIndexLeftProperties, null);
        TupleListEntry<ExistsCounter<LeftTuple_>> counterEntry = leftTuple.getStore(inputStoreIndexLeftCounterEntry);
        ExistsCounter<LeftTuple_> counter = counterEntry.getElement();

        indexerLeft.remove(indexProperties, counterEntry);
        if (isFiltering) {
            TupleList<FilteringTracker> leftTrackerList = leftTuple.getStore(inputStoreIndexLeftTrackerList);
            leftTrackerList.forEach(FilteringTracker::remove);
        }
        if (counter.isAlive()) {
            retractCounter(counter);
        }
    }

    @Override
    public final void insertRight(UniTuple<Right_> rightTuple) {
        if (rightTuple.getStore(inputStoreIndexRightProperties) != null) {
            throw new IllegalStateException("Impossible state: the input for the tuple (" + rightTuple
                    + ") was already added in the tupleStore.");
        }
        IndexProperties indexProperties = mappingRight.apply(rightTuple.getFactA());
        rightTuple.setStore(inputStoreIndexRightProperties, indexProperties);

        TupleListEntry<UniTuple<Right_>> rightEntry = indexerRight.put(indexProperties, rightTuple);
        rightTuple.setStore(inputStoreIndexRightEntry, rightEntry);
        if (!isFiltering) {
            indexerLeft.visit(indexProperties, counterEntry -> incrementCounter(counterEntry.getElement()));
        } else {
            TupleList<FilteringTracker> rightTrackerList = new TupleList<>();
            indexerLeft.visit(indexProperties, counterEntry -> {
                ExistsCounter<LeftTuple_> counter = counterEntry.getElement();
                if (testFiltering(counter.leftTuple, rightTuple)) {
                    incrementCounter(counter);
                    TupleList<FilteringTracker> leftTrackerList = counter.leftTuple.getStore(inputStoreIndexLeftTrackerList);
                    new FilteringTracker(counter, rightTuple, leftTrackerList, rightTrackerList);
                }
            });
            rightTuple.setStore(inputStoreIndexRightTrackerList, rightTrackerList);
        }
    }

    @Override
    public final void updateRight(UniTuple<Right_> rightTuple) {
        IndexProperties oldIndexProperties = rightTuple.getStore(inputStoreIndexRightProperties);
        if (oldIndexProperties == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            insertRight(rightTuple);
            return;
        }
        IndexProperties newIndexProperties = mappingRight.apply(rightTuple.getFactA());

        if (oldIndexProperties.equals(newIndexProperties)) {
            // No need for re-indexing because the index properties didn't change
            if (isFiltering) {
                TupleList<FilteringTracker> rightTrackerList = rightTuple.getStore(inputStoreIndexRightTrackerList);
                rightTrackerList.forEach(filteringTacker -> {
                    decrementCounter(filteringTacker.counter);
                    filteringTacker.remove();
                });
                indexerLeft.visit(oldIndexProperties, counterEntry -> {
                    ExistsCounter<LeftTuple_> counter = counterEntry.getElement();
                    if (testFiltering(counter.leftTuple, rightTuple)) {
                        incrementCounter(counter);
                        TupleList<FilteringTracker> leftTrackerList =
                                counter.leftTuple.getStore(inputStoreIndexLeftTrackerList);
                        new FilteringTracker(counter, rightTuple, leftTrackerList, rightTrackerList);
                    }
                });
            }
        } else {
            TupleListEntry<UniTuple<Right_>> rightEntry = rightTuple.getStore(inputStoreIndexRightEntry);
            indexerRight.remove(oldIndexProperties, rightEntry);
            if (!isFiltering) {
                indexerLeft.visit(oldIndexProperties, counterEntry -> decrementCounter(counterEntry.getElement()));
            } else {
                TupleList<FilteringTracker> rightTrackerList = rightTuple.getStore(inputStoreIndexRightTrackerList);
                rightTrackerList.forEach(filteringTacker -> {
                    decrementCounter(filteringTacker.counter);
                    filteringTacker.remove();
                });
            }
            rightTuple.setStore(inputStoreIndexRightProperties, newIndexProperties);
            rightEntry = indexerRight.put(newIndexProperties, rightTuple);
            rightTuple.setStore(inputStoreIndexRightEntry, rightEntry);
            if (!isFiltering) {
                indexerLeft.visit(newIndexProperties, counterEntry -> incrementCounter(counterEntry.getElement()));
            } else {
                TupleList<FilteringTracker> rightTrackerList = new TupleList<>();
                indexerLeft.visit(newIndexProperties, counterEntry -> {
                    ExistsCounter<LeftTuple_> counter = counterEntry.getElement();
                    if (testFiltering(counter.leftTuple, rightTuple)) {
                        incrementCounter(counter);
                        TupleList<FilteringTracker> leftTrackerList =
                                counter.leftTuple.getStore(inputStoreIndexLeftTrackerList);
                        new FilteringTracker(counter, rightTuple, leftTrackerList, rightTrackerList);
                    }
                });
                rightTuple.setStore(inputStoreIndexRightTrackerList, rightTrackerList);
            }
        }
    }

    @Override
    public final void retractRight(UniTuple<Right_> rightTuple) {
        IndexProperties indexProperties = rightTuple.getStore(inputStoreIndexRightProperties);
        if (indexProperties == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            return;
        }
        rightTuple.setStore(inputStoreIndexRightProperties, null);
        TupleListEntry<UniTuple<Right_>> rightEntry = rightTuple.getStore(inputStoreIndexRightEntry);
        indexerRight.remove(indexProperties, rightEntry);
        rightTuple.setStore(inputStoreIndexRightEntry, null);
        if (!isFiltering) {
            indexerLeft.visit(indexProperties, counterEntry -> decrementCounter(counterEntry.getElement()));
        } else {
            TupleList<FilteringTracker> rightTrackerList = rightTuple.getStore(inputStoreIndexRightTrackerList);
            rightTrackerList.forEach(filteringTacker -> {
                decrementCounter(filteringTacker.counter);
                filteringTacker.remove();
            });
        }
    }

    protected abstract IndexProperties createIndexProperties(LeftTuple_ leftTuple);

}
