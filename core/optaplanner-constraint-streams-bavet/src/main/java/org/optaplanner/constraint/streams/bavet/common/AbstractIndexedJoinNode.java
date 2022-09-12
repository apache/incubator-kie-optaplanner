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
 *
 * @param <LeftTuple_>
 * @param <Right_>
 */
public abstract class AbstractIndexedJoinNode<LeftTuple_ extends Tuple, Right_, OutTuple_ extends Tuple, MutableOutTuple_ extends OutTuple_>
        extends AbstractJoinNode<LeftTuple_, Right_, OutTuple_, MutableOutTuple_>
        implements LeftTupleLifecycle<LeftTuple_>, RightTupleLifecycle<UniTuple<Right_>> {

    private final Function<Right_, IndexProperties> mappingRight;

    private final int inputStoreIndexLeftProperties;
    private final int inputStoreIndexLeftEntry;
    private final int inputStoreIndexLeftOutTupleList;
    private final int inputStoreIndexRightProperties;
    private final int inputStoreIndexRightEntry;
    private final int inputStoreIndexRightOutTupleList;

    private final int outputStoreIndexLeftOutEntry;
    private final int outputStoreIndexRightOutEntry;

    /**
     * Calls for example {@link AbstractScorer#insert(Tuple)} and/or ...
     */
    private final Indexer<LeftTuple_, Void> indexerLeft;
    private final Indexer<UniTuple<Right_>, Void> indexerRight;

    protected AbstractIndexedJoinNode(Function<Right_, IndexProperties> mappingRight,
            int inputStoreIndexLeftProperties, int inputStoreIndexLeftEntry, int inputStoreIndexLeftOutTupleList,
            int inputStoreIndexRightProperties, int inputStoreIndexRightEntry, int inputStoreIndexRightOutTupleList,
            TupleLifecycle<OutTuple_> nextNodesTupleLifecycle,
            int outputStoreIndexLeftOutEntry, int outputStoreIndexRightOutEntry,
            Indexer<LeftTuple_, Void> indexerLeft, Indexer<UniTuple<Right_>, Void> indexerRight) {
        super(nextNodesTupleLifecycle);
        this.mappingRight = mappingRight;
        this.inputStoreIndexLeftProperties = inputStoreIndexLeftProperties;
        this.inputStoreIndexLeftEntry = inputStoreIndexLeftEntry;
        this.inputStoreIndexLeftOutTupleList = inputStoreIndexLeftOutTupleList;
        this.inputStoreIndexRightProperties = inputStoreIndexRightProperties;
        this.inputStoreIndexRightEntry = inputStoreIndexRightEntry;
        this.inputStoreIndexRightOutTupleList = inputStoreIndexRightOutTupleList;
        this.outputStoreIndexLeftOutEntry = outputStoreIndexLeftOutEntry;
        this.outputStoreIndexRightOutEntry = outputStoreIndexRightOutEntry;
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
        leftTuple.setStore(inputStoreIndexLeftProperties, indexProperties);

        TupleListEntry<LeftTuple_> leftEntry = indexerLeft.putGGG(indexProperties, leftTuple);
        leftTuple.setStore(inputStoreIndexLeftEntry, leftEntry);
        TupleList<MutableOutTuple_> outTupleListLeft = new TupleList<>();
        leftTuple.setStore(inputStoreIndexLeftOutTupleList, outTupleListLeft);
        indexerRight.visitGGG(indexProperties, (rightEntry) -> {
            UniTuple<Right_> rightTuple = rightEntry.getTuple();
            MutableOutTuple_ outTuple = createOutTuple(leftTuple, rightTuple);
            TupleListEntry<MutableOutTuple_> outEntryLeft = outTupleListLeft.add(outTuple);
            outTuple.setStore(outputStoreIndexLeftOutEntry, outEntryLeft);
            TupleList<MutableOutTuple_> outTupleListRight = rightTuple.getStore(inputStoreIndexRightOutTupleList);
            TupleListEntry<MutableOutTuple_> outEntryRight = outTupleListRight.add(outTuple);
            outTuple.setStore(outputStoreIndexRightOutEntry, outEntryRight);
            dirtyTupleQueue.add(outTuple);
        });
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
            // Still needed to propagate the update for downstream filters, matchWeighers, ...
            TupleList<MutableOutTuple_> outTupleListLeft = leftTuple.getStore(inputStoreIndexLeftOutTupleList);
            for (TupleListEntry<MutableOutTuple_> outEntryLeft = outTupleListLeft.first(); outEntryLeft != null; outEntryLeft =
                    outEntryLeft.next()) {
                MutableOutTuple_ outTuple = outEntryLeft.getTuple();
                updateOutTupleLeft(outTuple, leftTuple);
                updateTuple(outTuple);
            }
        } else {
            TupleListEntry<LeftTuple_> leftEntry = leftTuple.getStore(inputStoreIndexLeftEntry);
            indexerLeft.removeGGG(oldIndexProperties, leftEntry);
            TupleList<MutableOutTuple_> outTupleListLeft = leftTuple.getStore(inputStoreIndexLeftOutTupleList);
            for (TupleListEntry<MutableOutTuple_> outEntryLeft = outTupleListLeft.first(); outEntryLeft != null; outEntryLeft =
                    outEntryLeft.removeAndNext()) {
                MutableOutTuple_ outTuple = outEntryLeft.getTuple();
                TupleListEntry<MutableOutTuple_> outEntryRight = outTuple.getStore(outputStoreIndexRightOutEntry);
                outEntryRight.remove();
                outTuple.setStore(outputStoreIndexLeftOutEntry, null);
                outTuple.setStore(outputStoreIndexRightOutEntry, null);
                retractTuple(outTuple);
            }
            // outTupleListLeft is now empty

            leftTuple.setStore(inputStoreIndexLeftProperties, newIndexProperties);
            leftEntry = indexerLeft.putGGG(newIndexProperties, leftTuple);
            leftTuple.setStore(inputStoreIndexLeftEntry, leftEntry);
            // No need for leftTuple.setStore(inputStoreIndexLeftOutTupleList, outTupleListLeft);
            indexerRight.visitGGG(newIndexProperties, (rightEntry) -> {
                UniTuple<Right_> rightTuple = rightEntry.getTuple();
                MutableOutTuple_ outTuple = createOutTuple(leftTuple, rightTuple);
                TupleListEntry<MutableOutTuple_> outEntryLeft = outTupleListLeft.add(outTuple);
                outTuple.setStore(outputStoreIndexLeftOutEntry, outEntryLeft);
                TupleList<MutableOutTuple_> outTupleListRight = rightTuple.getStore(inputStoreIndexRightOutTupleList);
                TupleListEntry<MutableOutTuple_> outEntryRight = outTupleListRight.add(outTuple);
                outTuple.setStore(outputStoreIndexRightOutEntry, outEntryRight);
                dirtyTupleQueue.add(outTuple);
            });
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
        TupleListEntry<LeftTuple_> leftEntry = leftTuple.getStore(inputStoreIndexLeftEntry);
        leftTuple.setStore(inputStoreIndexLeftEntry, null);
        TupleList<MutableOutTuple_> outTupleListLeft = leftTuple.getStore(inputStoreIndexLeftOutTupleList);
        leftTuple.setStore(inputStoreIndexLeftOutTupleList, null);

        indexerLeft.removeGGG(indexProperties, leftEntry);
        // No need for outEntryLeft.removeAndNext(); because outTupleListLeft is garbage collected
        for (TupleListEntry<MutableOutTuple_> outEntryLeft = outTupleListLeft.first(); outEntryLeft != null; outEntryLeft =
                outEntryLeft.next()) {
            MutableOutTuple_ outTuple = outEntryLeft.getTuple();
            TupleListEntry<MutableOutTuple_> outEntryRight = outTuple.getStore(outputStoreIndexRightOutEntry);
            outEntryRight.remove();
            outTuple.setStore(outputStoreIndexLeftOutEntry, null);
            outTuple.setStore(outputStoreIndexRightOutEntry, null);
            retractTuple(outTuple);
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

        TupleListEntry<UniTuple<Right_>> rightEntry = indexerRight.putGGG(indexProperties, rightTuple);
        rightTuple.setStore(inputStoreIndexRightEntry, rightEntry);
        TupleList<MutableOutTuple_> outTupleListRight = new TupleList<>();
        rightTuple.setStore(inputStoreIndexRightOutTupleList, outTupleListRight);
        indexerLeft.visitGGG(indexProperties, (leftEntry) -> {
            LeftTuple_ leftTuple = leftEntry.getTuple();
            MutableOutTuple_ outTuple = createOutTuple(leftTuple, rightTuple);
            TupleListEntry<MutableOutTuple_> outEntryRight = outTupleListRight.add(outTuple);
            outTuple.setStore(outputStoreIndexRightOutEntry, outEntryRight);
            TupleList<MutableOutTuple_> outTupleListLeft = leftTuple.getStore(inputStoreIndexLeftOutTupleList);
            TupleListEntry<MutableOutTuple_> outEntryLeft = outTupleListLeft.add(outTuple);
            outTuple.setStore(outputStoreIndexLeftOutEntry, outEntryLeft);
            dirtyTupleQueue.add(outTuple);
        });
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
            // Still needed to propagate the update for downstream filters, matchWeighers, ...
            TupleList<MutableOutTuple_> outTupleListRight = rightTuple.getStore(inputStoreIndexRightOutTupleList);
            for (TupleListEntry<MutableOutTuple_> outEntryRight =
                    outTupleListRight.first(); outEntryRight != null; outEntryRight = outEntryRight.next()) {
                MutableOutTuple_ outTuple = outEntryRight.getTuple();
                updateOutTupleRight(outTuple, rightTuple);
                updateTuple(outTuple);
            }
        } else {
            TupleListEntry<UniTuple<Right_>> rightEntry = rightTuple.getStore(inputStoreIndexRightEntry);
            indexerRight.removeGGG(oldIndexProperties, rightEntry);
            TupleList<MutableOutTuple_> outTupleListRight = rightTuple.getStore(inputStoreIndexRightOutTupleList);
            for (TupleListEntry<MutableOutTuple_> outEntryRight =
                    outTupleListRight.first(); outEntryRight != null; outEntryRight = outEntryRight.removeAndNext()) {
                MutableOutTuple_ outTuple = outEntryRight.getTuple();
                TupleListEntry<MutableOutTuple_> outEntryLeft = outTuple.getStore(outputStoreIndexLeftOutEntry);
                outEntryLeft.remove();
                outTuple.setStore(outputStoreIndexRightOutEntry, null);
                outTuple.setStore(outputStoreIndexLeftOutEntry, null);
                retractTuple(outTuple);
            }
            // outTupleListRight is now empty

            rightTuple.setStore(inputStoreIndexRightProperties, newIndexProperties);
            rightEntry = indexerRight.putGGG(newIndexProperties, rightTuple);
            rightTuple.setStore(inputStoreIndexRightEntry, rightEntry);
            // No need for rightTuple.setStore(inputStoreIndexRightOutTupleList, outTupleListRight);
            indexerLeft.visitGGG(newIndexProperties, (leftEntry) -> {
                LeftTuple_ leftTuple = leftEntry.getTuple();
                MutableOutTuple_ outTuple = createOutTuple(leftTuple, rightTuple);
                TupleListEntry<MutableOutTuple_> outEntryRight = outTupleListRight.add(outTuple);
                outTuple.setStore(outputStoreIndexRightOutEntry, outEntryRight);
                TupleList<MutableOutTuple_> outTupleListLeft = leftTuple.getStore(inputStoreIndexLeftOutTupleList);
                TupleListEntry<MutableOutTuple_> outEntryLeft = outTupleListLeft.add(outTuple);
                outTuple.setStore(outputStoreIndexLeftOutEntry, outEntryLeft);
                dirtyTupleQueue.add(outTuple);
            });
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
        rightTuple.setStore(inputStoreIndexRightEntry, null);
        TupleList<MutableOutTuple_> outTupleListRight = rightTuple.getStore(inputStoreIndexRightOutTupleList);
        rightTuple.setStore(inputStoreIndexRightOutTupleList, null);

        indexerRight.removeGGG(indexProperties, rightEntry);
        // No need for outEntryRight.removeAndNext(); because outTupleListRight is garbage collected
        for (TupleListEntry<MutableOutTuple_> outEntryRight = outTupleListRight.first(); outEntryRight != null; outEntryRight =
                outEntryRight.next()) {
            MutableOutTuple_ outTuple = outEntryRight.getTuple();
            TupleListEntry<MutableOutTuple_> outEntryLeft = outTuple.getStore(outputStoreIndexLeftOutEntry);
            outEntryLeft.remove();
            outTuple.setStore(outputStoreIndexRightOutEntry, null);
            outTuple.setStore(outputStoreIndexLeftOutEntry, null);
            retractTuple(outTuple);
        }
    }

    protected abstract IndexProperties createIndexPropertiesLeft(LeftTuple_ leftTuple);

}
