package org.optaplanner.constraint.streams.bavet.common;

import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.collection.TupleList;
import org.optaplanner.constraint.streams.bavet.common.collection.TupleListEntry;
import org.optaplanner.constraint.streams.bavet.common.index.IndexProperties;
import org.optaplanner.constraint.streams.bavet.common.index.Indexer;
import org.optaplanner.constraint.streams.bavet.uni.UniTuple;

/**
 * There is a strong likelihood that any change to this class, which is not related to indexing,
 * should also be made to {@link AbstractUnindexedJoinNode}.
 * <p>
 * This class also has a child ({@link AbstractFilteredIndexedJoinNode}) which adds logic regarding filtering.
 * It is possible that changes to this class will also require changes to the child.
 *
 * @param <LeftTuple_>
 * @param <Right_>
 */
public abstract class AbstractIndexedJoinNode<LeftTuple_ extends Tuple, Right_, OutTuple_ extends Tuple, MutableOutTuple_ extends OutTuple_>
        extends AbstractJoinNode<LeftTuple_, Right_, OutTuple_, MutableOutTuple_> {

    private final Function<Right_, IndexProperties> mappingRight;
    protected final int inputStoreIndexLeftProperties;
    private final int inputStoreIndexLeftEntry;
    protected final int inputStoreIndexRightProperties;
    private final int inputStoreIndexRightEntry;
    /**
     * Calls for example {@link AbstractScorer#insert(Tuple)} and/or ...
     */
    protected final Indexer<LeftTuple_> indexerLeft;
    protected final Indexer<UniTuple<Right_>> indexerRight;

    protected AbstractIndexedJoinNode(Function<Right_, IndexProperties> mappingRight,
            int inputStoreIndexLeftProperties, int inputStoreIndexLeftEntry, int inputStoreIndexLeftOutTupleList,
            int inputStoreIndexRightProperties, int inputStoreIndexRightEntry, int inputStoreIndexRightOutTupleList,
            TupleLifecycle<OutTuple_> nextNodesTupleLifecycle, int outputStoreIndexLeftOutEntry,
            int outputStoreIndexRightOutEntry,
            Indexer<LeftTuple_> indexerLeft, Indexer<UniTuple<Right_>> indexerRight) {
        super(inputStoreIndexLeftOutTupleList, inputStoreIndexRightOutTupleList, nextNodesTupleLifecycle,
                outputStoreIndexLeftOutEntry, outputStoreIndexRightOutEntry);
        this.mappingRight = mappingRight;
        this.inputStoreIndexLeftProperties = inputStoreIndexLeftProperties;
        this.inputStoreIndexLeftEntry = inputStoreIndexLeftEntry;
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
        IndexProperties indexProperties = createIndexPropertiesLeft(leftTuple);

        TupleList<MutableOutTuple_> outTupleListLeft = new TupleList<>();
        leftTuple.setStore(inputStoreIndexLeftOutTupleList, outTupleListLeft);
        indexAndPropagateLeft(leftTuple, indexProperties);
    }

    @Override
    public final void updateLeft(LeftTuple_ leftTuple) {
        IndexProperties oldIndexProperties = leftTuple.getStore(inputStoreIndexLeftProperties);
        if (oldIndexProperties == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            insertLeft(leftTuple);
            return;
        }
        IndexProperties newIndexProperties = createIndexPropertiesLeft(leftTuple);
        if (oldIndexProperties.equals(newIndexProperties)) {
            // No need for re-indexing because the index properties didn't change
            // Prefer an update over retract-insert if possible
            TupleList<MutableOutTuple_> outTupleListLeft = leftTuple.getStore(inputStoreIndexLeftOutTupleList);
            updateOutTupleLeftMaybeFiltering(outTupleListLeft, leftTuple);
        } else {
            TupleListEntry<LeftTuple_> leftEntry = leftTuple.getStore(inputStoreIndexLeftEntry);
            TupleList<MutableOutTuple_> outTupleListLeft = leftTuple.getStore(inputStoreIndexLeftOutTupleList);
            indexerLeft.remove(oldIndexProperties, leftEntry);
            outTupleListLeft.forEach(this::retractOutTuple);
            // outTupleListLeft is now empty
            // No need for leftTuple.setStore(inputStoreIndexLeftOutTupleList, outTupleListLeft);
            indexAndPropagateLeft(leftTuple, newIndexProperties);
        }
    }

    protected void updateOutTupleLeftMaybeFiltering(TupleList<MutableOutTuple_> outTupleListLeft, LeftTuple_ leftTuple) {
        // Propagate the update for downstream filters, matchWeighers, ...
        outTupleListLeft.forEach(outTuple -> updateOutTupleLeft(outTuple, leftTuple));
    }

    private void indexAndPropagateLeft(LeftTuple_ leftTuple, IndexProperties indexProperties) {
        leftTuple.setStore(inputStoreIndexLeftProperties, indexProperties);
        TupleListEntry<LeftTuple_> leftEntry = indexerLeft.put(indexProperties, leftTuple);
        leftTuple.setStore(inputStoreIndexLeftEntry, leftEntry);
        indexerRight.forEach(indexProperties, (rightTuple) -> {
            if (testFiltering(leftTuple, rightTuple)) {
                insertOutTuple(leftTuple, rightTuple);
            }
        });
    }

    @Override
    public final void retractLeft(LeftTuple_ leftTuple) {
        IndexProperties indexProperties = leftTuple.removeStore(inputStoreIndexLeftProperties);
        if (indexProperties == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            return;
        }
        TupleListEntry<LeftTuple_> leftEntry = leftTuple.removeStore(inputStoreIndexLeftEntry);
        TupleList<MutableOutTuple_> outTupleListLeft = leftTuple.removeStore(inputStoreIndexLeftOutTupleList);
        indexerLeft.remove(indexProperties, leftEntry);
        outTupleListLeft.forEach(this::retractOutTuple);
    }

    @Override
    public final void insertRight(UniTuple<Right_> rightTuple) {
        if (rightTuple.getStore(inputStoreIndexRightProperties) != null) {
            throw new IllegalStateException("Impossible state: the input for the tuple (" + rightTuple
                    + ") was already added in the tupleStore.");
        }
        IndexProperties indexProperties = mappingRight.apply(rightTuple.getFactA());

        TupleList<MutableOutTuple_> outTupleListRight = new TupleList<>();
        rightTuple.setStore(inputStoreIndexRightOutTupleList, outTupleListRight);
        indexAndPropagateRight(rightTuple, indexProperties);
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
            // Prefer an update over retract-insert if possible
            TupleList<MutableOutTuple_> outTupleListRight = rightTuple.getStore(inputStoreIndexRightOutTupleList);
            updateOutTupleRightMaybeFiltering(outTupleListRight, rightTuple);
        } else {
            TupleListEntry<UniTuple<Right_>> rightEntry = rightTuple.getStore(inputStoreIndexRightEntry);
            TupleList<MutableOutTuple_> outTupleListRight = rightTuple.getStore(inputStoreIndexRightOutTupleList);
            indexerRight.remove(oldIndexProperties, rightEntry);
            outTupleListRight.forEach(this::retractOutTuple);
            // outTupleListRight is now empty
            // No need for rightTuple.setStore(inputStoreIndexRightOutTupleList, outTupleListRight);
            indexAndPropagateRight(rightTuple, newIndexProperties);
        }
    }

    protected void updateOutTupleRightMaybeFiltering(TupleList<MutableOutTuple_> outTupleListRight,
            UniTuple<Right_> rightTuple) {
        // Propagate the update for downstream filters, matchWeighers, ...
        outTupleListRight.forEach(outTuple -> updateOutTupleRight(outTuple, rightTuple));
    }

    private void indexAndPropagateRight(UniTuple<Right_> rightTuple, IndexProperties indexProperties) {
        rightTuple.setStore(inputStoreIndexRightProperties, indexProperties);
        TupleListEntry<UniTuple<Right_>> rightEntry = indexerRight.put(indexProperties, rightTuple);
        rightTuple.setStore(inputStoreIndexRightEntry, rightEntry);
        indexerLeft.forEach(indexProperties, (leftTuple) -> {
            if (testFiltering(leftTuple, rightTuple)) {
                insertOutTuple(leftTuple, rightTuple);
            }
        });
    }

    @Override
    public final void retractRight(UniTuple<Right_> rightTuple) {
        IndexProperties indexProperties = rightTuple.removeStore(inputStoreIndexRightProperties);
        if (indexProperties == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            return;
        }
        TupleListEntry<UniTuple<Right_>> rightEntry = rightTuple.removeStore(inputStoreIndexRightEntry);
        TupleList<MutableOutTuple_> outTupleListRight = rightTuple.removeStore(inputStoreIndexRightOutTupleList);
        indexerRight.remove(indexProperties, rightEntry);
        outTupleListRight.forEach(this::retractOutTuple);
    }

    protected abstract IndexProperties createIndexPropertiesLeft(LeftTuple_ leftTuple);

}
