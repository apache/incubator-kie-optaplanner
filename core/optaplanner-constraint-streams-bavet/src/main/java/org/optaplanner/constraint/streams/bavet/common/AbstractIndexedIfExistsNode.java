package org.optaplanner.constraint.streams.bavet.common;

import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.collection.TupleListEntry;
import org.optaplanner.constraint.streams.bavet.common.index.IndexProperties;
import org.optaplanner.constraint.streams.bavet.common.index.Indexer;
import org.optaplanner.constraint.streams.bavet.uni.UniTuple;

/**
 * There is a strong likelihood that any change to this class, which is not related to indexing,
 * should also be made to {@link AbstractUnindexedIfExistsNode}.
 * <p>
 * This class also has a child ({@link AbstractFilteredIndexedIfExistsNode}) which adds logic regarding filtering.
 * It is possible that changes to this class will also require changes to the child.
 *
 * @param <LeftTuple_>
 * @param <Right_>
 */
public abstract class AbstractIndexedIfExistsNode<LeftTuple_ extends Tuple, Right_>
        extends AbstractIfExistsNode<LeftTuple_, Right_> {

    private final Function<Right_, IndexProperties> mappingRight;
    protected final int inputStoreIndexLeftProperties;
    protected final int inputStoreIndexLeftCounterEntry;
    protected final int inputStoreIndexRightProperties;
    protected final int inputStoreIndexRightEntry;
    protected final Indexer<ExistsCounter<LeftTuple_>> indexerLeft;
    protected final Indexer<UniTuple<Right_>> indexerRight;

    protected AbstractIndexedIfExistsNode(boolean shouldExist,
            Function<Right_, IndexProperties> mappingRight,
            int inputStoreIndexLeftProperties, int inputStoreIndexLeftCounterEntry, int inputStoreIndexLeftTrackerList,
            int inputStoreIndexRightProperties, int inputStoreIndexRightEntry, int inputStoreIndexRightTrackerList,
            TupleLifecycle<LeftTuple_> nextNodesTupleLifecycle,
            Indexer<ExistsCounter<LeftTuple_>> indexerLeft,
            Indexer<UniTuple<Right_>> indexerRight) {
        super(shouldExist, inputStoreIndexLeftTrackerList, inputStoreIndexRightTrackerList,
                nextNodesTupleLifecycle);
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

        ExistsCounter<LeftTuple_> counter = new ExistsCounter<>(leftTuple);
        TupleListEntry<ExistsCounter<LeftTuple_>> counterEntry = indexerLeft.put(indexProperties, counter);
        leftTuple.setStore(inputStoreIndexLeftCounterEntry, counterEntry);

        insertLeftMaybeFiltering(counter, leftTuple, indexProperties);
        initCounterLeft(counter);
    }

    protected void insertLeftMaybeFiltering(ExistsCounter<LeftTuple_> counter, LeftTuple_ leftTuple,
            IndexProperties indexProperties) {
        counter.countRight = indexerRight.size(indexProperties);
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
            updateLeftWithoutReindex(counter, leftTuple, oldIndexProperties);
        } else {
            updateLeftWithReindex(counter, counterEntry, leftTuple, oldIndexProperties, newIndexProperties);
        }
    }

    protected void updateLeftWithoutReindex(ExistsCounter<LeftTuple_> counter, LeftTuple_ leftTuple,
            IndexProperties oldIndexProperties) {
        updateUnchangedCounterLeft(counter);
    }

    protected void updateLeftWithReindex(ExistsCounter<LeftTuple_> counter,
            TupleListEntry<ExistsCounter<LeftTuple_>> counterEntry, LeftTuple_ leftTuple, IndexProperties oldIndexProperties,
            IndexProperties newIndexProperties) {
        indexerLeft.remove(oldIndexProperties, counterEntry);
        counter.countRight = 0;
        leftTuple.setStore(inputStoreIndexLeftProperties, newIndexProperties);
        counterEntry = indexerLeft.put(newIndexProperties, counter);
        leftTuple.setStore(inputStoreIndexLeftCounterEntry, counterEntry);
        counter.countRight = indexerRight.size(newIndexProperties);
        updateCounterLeft(counter);
    }

    @Override
    public final void retractLeft(LeftTuple_ leftTuple) {
        IndexProperties indexProperties = leftTuple.removeStore(inputStoreIndexLeftProperties);
        if (indexProperties == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            return;
        }
        TupleListEntry<ExistsCounter<LeftTuple_>> counterEntry = leftTuple.getStore(inputStoreIndexLeftCounterEntry);
        ExistsCounter<LeftTuple_> counter = counterEntry.getElement();

        indexerLeft.remove(indexProperties, counterEntry);
        retractLeftMaybeFiltering(leftTuple);
        killCounterLeft(counter);
    }

    protected void retractLeftMaybeFiltering(LeftTuple_ leftTuple) {
        // Intentionally empty.
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
        insertRightMaybeFiltering(rightTuple, indexProperties);
    }

    protected void insertRightMaybeFiltering(UniTuple<Right_> rightTuple, IndexProperties indexProperties) {
        indexerLeft.forEach(indexProperties, this::incrementCounterRight);
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
            updateRightWithoutReindex(rightTuple, oldIndexProperties);
        } else {
            updateRightWithReindex(rightTuple, oldIndexProperties, newIndexProperties);
        }
    }

    protected void updateRightWithoutReindex(UniTuple<Right_> rightTuple, IndexProperties oldIndexProperties) {
        // Intentionally empty.
    }

    protected void updateRightWithReindex(UniTuple<Right_> rightTuple, IndexProperties oldIndexProperties,
            IndexProperties newIndexProperties) {
        TupleListEntry<UniTuple<Right_>> rightEntry = rightTuple.getStore(inputStoreIndexRightEntry);
        indexerRight.remove(oldIndexProperties, rightEntry);
        indexerLeft.forEach(oldIndexProperties, this::decrementCounterRight);
        rightTuple.setStore(inputStoreIndexRightProperties, newIndexProperties);
        rightEntry = indexerRight.put(newIndexProperties, rightTuple);
        rightTuple.setStore(inputStoreIndexRightEntry, rightEntry);
        indexerLeft.forEach(newIndexProperties, this::incrementCounterRight);
    }

    @Override
    public final void retractRight(UniTuple<Right_> rightTuple) {
        IndexProperties indexProperties = rightTuple.removeStore(inputStoreIndexRightProperties);
        if (indexProperties == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            return;
        }
        TupleListEntry<UniTuple<Right_>> rightEntry = rightTuple.removeStore(inputStoreIndexRightEntry);
        indexerRight.remove(indexProperties, rightEntry);
        retractRightMaybeFiltering(rightTuple, indexProperties);
    }

    protected void retractRightMaybeFiltering(UniTuple<Right_> rightTuple, IndexProperties indexProperties) {
        indexerLeft.forEach(indexProperties, this::decrementCounterRight);
    }

    protected abstract IndexProperties createIndexProperties(LeftTuple_ leftTuple);

}
