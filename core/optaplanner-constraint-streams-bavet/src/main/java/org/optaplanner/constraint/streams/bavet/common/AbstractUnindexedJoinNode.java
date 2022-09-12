package org.optaplanner.constraint.streams.bavet.common;

import org.optaplanner.constraint.streams.bavet.common.collection.TupleList;
import org.optaplanner.constraint.streams.bavet.common.collection.TupleListEntry;
import org.optaplanner.constraint.streams.bavet.uni.UniTuple;

/**
 * There is a strong likelihood that any change made to this class
 * should also be made to {@link AbstractIndexedJoinNode}.
 *
 * @param <LeftTuple_>
 * @param <Right_>
 */
public abstract class AbstractUnindexedJoinNode<LeftTuple_ extends Tuple, Right_, OutTuple_ extends Tuple, MutableOutTuple_ extends OutTuple_>
        extends AbstractJoinNode<LeftTuple_, Right_, OutTuple_, MutableOutTuple_>
        implements LeftTupleLifecycle<LeftTuple_>, RightTupleLifecycle<UniTuple<Right_>> {

    private final int inputStoreIndexLeftEntry;
    private final int inputStoreIndexLeftOutTupleList;
    private final int inputStoreIndexRightEntry;
    private final int inputStoreIndexRightOutTupleList;

    private final int outputStoreIndexLeftOutEntry;
    private final int outputStoreIndexRightOutEntry;

    private final TupleList<LeftTuple_> leftTupleList = new TupleList<>();
    private final TupleList<UniTuple<Right_>> rightTupleList = new TupleList<>();

    protected AbstractUnindexedJoinNode(
            int inputStoreIndexLeftEntry, int inputStoreIndexLeftOutTupleList,
            int inputStoreIndexRightEntry, int inputStoreIndexRightOutTupleList,
            TupleLifecycle<OutTuple_> nextNodesTupleLifecycle,
            int outputStoreIndexLeftOutEntry, int outputStoreIndexRightOutEntry) {
        super(nextNodesTupleLifecycle);
        this.inputStoreIndexLeftEntry = inputStoreIndexLeftEntry;
        this.inputStoreIndexLeftOutTupleList = inputStoreIndexLeftOutTupleList;
        this.inputStoreIndexRightEntry = inputStoreIndexRightEntry;
        this.inputStoreIndexRightOutTupleList = inputStoreIndexRightOutTupleList;
        this.outputStoreIndexLeftOutEntry = outputStoreIndexLeftOutEntry;
        this.outputStoreIndexRightOutEntry = outputStoreIndexRightOutEntry;
    }

    @Override
    public final void insertLeft(LeftTuple_ leftTuple) {
        if (leftTuple.getStore(inputStoreIndexLeftEntry) != null) {
            throw new IllegalStateException("Impossible state: the input for the tuple (" + leftTuple
                    + ") was already added in the tupleStore.");
        }
        TupleListEntry<LeftTuple_> leftEntry = leftTupleList.add(leftTuple);
        leftTuple.setStore(inputStoreIndexLeftEntry, leftEntry);
        TupleList<MutableOutTuple_> outTupleListLeft = new TupleList<>();
        leftTuple.setStore(inputStoreIndexLeftOutTupleList, outTupleListLeft);
        for (TupleListEntry<UniTuple<Right_>> rightEntry = rightTupleList.first();
                rightEntry != null; rightEntry = rightEntry.next()) {
            UniTuple<Right_> rightTuple = rightEntry.getTuple();
            MutableOutTuple_ outTuple = createOutTuple(leftTuple, rightTuple);
            TupleListEntry<MutableOutTuple_> outEntryLeft = outTupleListLeft.add(outTuple);
            outTuple.setStore(outputStoreIndexLeftOutEntry, outEntryLeft);
            TupleList<MutableOutTuple_> outTupleListRight = rightTuple.getStore(inputStoreIndexRightOutTupleList);
            TupleListEntry<MutableOutTuple_> outEntryRight = outTupleListRight.add(outTuple);
            outTuple.setStore(outputStoreIndexRightOutEntry, outEntryRight);
            dirtyTupleQueue.add(outTuple);
        }
    }

    @Override
    public final void updateLeft(LeftTuple_ leftTuple) {
        TupleListEntry<LeftTuple_> leftEntry = leftTuple.getStore(inputStoreIndexLeftEntry);
        if (leftEntry == null) { // We don't track which tuples made it through the filter predicate(s).
            insertLeft(leftTuple);
            return;
        }
        TupleList<MutableOutTuple_> outTupleListLeft = leftTuple.getStore(inputStoreIndexLeftOutTupleList);
        for (TupleListEntry<MutableOutTuple_> outEntryLeft = outTupleListLeft.first();
                outEntryLeft != null; outEntryLeft = outEntryLeft.next()) {
            MutableOutTuple_ outTuple = outEntryLeft.getTuple();
            updateOutTupleLeft(outTuple, leftTuple);
            updateTuple(outTuple);
        }
    }

    @Override
    public final void retractLeft(LeftTuple_ leftTuple) {
        TupleListEntry<LeftTuple_> leftEntry = leftTuple.getStore(inputStoreIndexLeftEntry);
        if (leftEntry == null) { // We don't track which tuples made it through the filter predicate(s).
            return;
        }
        leftTuple.setStore(inputStoreIndexLeftEntry, null);
        TupleList<MutableOutTuple_> outTupleListLeft = leftTuple.getStore(inputStoreIndexLeftOutTupleList);
        leftTuple.setStore(inputStoreIndexLeftOutTupleList, null);

        leftEntry.remove();
        // No need for outEntryLeft.removeAndNext(); because outTupleListLeft is garbage collected
        for (TupleListEntry<MutableOutTuple_> outEntryLeft = outTupleListLeft.first();
                outEntryLeft != null; outEntryLeft = outEntryLeft.next()) {
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
        if (rightTuple.getStore(inputStoreIndexRightEntry) != null) {
            throw new IllegalStateException("Impossible state: the input for the tuple (" + rightTuple
                    + ") was already added in the tupleStore.");
        }
        TupleListEntry<UniTuple<Right_>> rightEntry = rightTupleList.add(rightTuple);
        rightTuple.setStore(inputStoreIndexRightEntry, rightEntry);
        TupleList<MutableOutTuple_> outTupleListRight = new TupleList<>();
        rightTuple.setStore(inputStoreIndexRightOutTupleList, outTupleListRight);
        for (TupleListEntry<LeftTuple_> leftEntry = leftTupleList.first();
                leftEntry != null; leftEntry = leftEntry.next()) {
            LeftTuple_ leftTuple = leftEntry.getTuple();
            MutableOutTuple_ outTuple = createOutTuple(leftTuple, rightTuple);
            TupleListEntry<MutableOutTuple_> outEntryRight = outTupleListRight.add(outTuple);
            outTuple.setStore(outputStoreIndexRightOutEntry, outEntryRight);
            TupleList<MutableOutTuple_> outTupleListLeft = leftTuple.getStore(inputStoreIndexLeftOutTupleList);
            TupleListEntry<MutableOutTuple_> outEntryLeft = outTupleListLeft.add(outTuple);
            outTuple.setStore(outputStoreIndexLeftOutEntry, outEntryLeft);
            dirtyTupleQueue.add(outTuple);
        }
    }

    @Override
    public final void updateRight(UniTuple<Right_> rightTuple) {
        TupleListEntry<UniTuple<Right_>> rightEntry = rightTuple.getStore(inputStoreIndexRightEntry);
        if (rightEntry == null) { // We don't track which tuples made it through the filter predicate(s).
            insertRight(rightTuple);
            return;
        }
        TupleList<MutableOutTuple_> outTupleListRight = rightTuple.getStore(inputStoreIndexRightOutTupleList);
        for (TupleListEntry<MutableOutTuple_> outEntryRight = outTupleListRight.first();
                outEntryRight != null; outEntryRight = outEntryRight.next()) {
            MutableOutTuple_ outTuple = outEntryRight.getTuple();
            updateOutTupleRight(outTuple, rightTuple);
            updateTuple(outTuple);
        }
    }

    @Override
    public final void retractRight(UniTuple<Right_> rightTuple) {
        TupleListEntry<UniTuple<Right_>> rightEntry = rightTuple.getStore(inputStoreIndexRightEntry);
        if (rightEntry == null) { // We don't track which tuples made it through the filter predicate(s).
            return;
        }
        rightTuple.setStore(inputStoreIndexRightEntry, null);
        TupleList<MutableOutTuple_> outTupleListRight = rightTuple.getStore(inputStoreIndexRightOutTupleList);
        rightTuple.setStore(inputStoreIndexRightOutTupleList, null);

        rightEntry.remove();
        // No need for outEntryRight.removeAndNext(); because outTupleListRight is garbage collected
        for (TupleListEntry<MutableOutTuple_> outEntryRight = outTupleListRight.first();
                outEntryRight != null; outEntryRight = outEntryRight.next()) {
            MutableOutTuple_ outTuple = outEntryRight.getTuple();
            TupleListEntry<MutableOutTuple_> outEntryLeft = outTuple.getStore(outputStoreIndexLeftOutEntry);
            outEntryLeft.remove();
            outTuple.setStore(outputStoreIndexRightOutEntry, null);
            outTuple.setStore(outputStoreIndexLeftOutEntry, null);
            retractTuple(outTuple);
        }
    }

}
