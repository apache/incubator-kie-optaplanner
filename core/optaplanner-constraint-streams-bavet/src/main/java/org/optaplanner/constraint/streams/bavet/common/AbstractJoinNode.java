package org.optaplanner.constraint.streams.bavet.common;

import org.optaplanner.constraint.streams.bavet.common.collection.TupleList;
import org.optaplanner.constraint.streams.bavet.common.collection.TupleListEntry;
import org.optaplanner.constraint.streams.bavet.uni.UniTuple;

/**
 * This class has two direct children: {@link AbstractIndexedJoinNode} and {@link AbstractUnindexedJoinNode}.
 * The logic in either is identical, except that the latter removes all indexing work.
 * Therefore any time that one of the classes changes,
 * the other should be inspected if it could benefit from applying the change there too.
 *
 * @param <LeftTuple_>
 * @param <Right_>
 */
public abstract class AbstractJoinNode<LeftTuple_ extends Tuple, Right_, OutTuple_ extends Tuple, MutableOutTuple_ extends OutTuple_>
        extends AbstractNode
        implements LeftTupleLifecycle<LeftTuple_>, RightTupleLifecycle<UniTuple<Right_>> {

    /**
     * Calls for example {@link AbstractScorer#insert(Tuple)} and/or ...
     */
    private final TupleLifecycle<OutTuple_> nextNodesTupleLifecycle;

    protected final int inputStoreIndexLeftOutTupleList;
    protected final int inputStoreIndexRightOutTupleList;

    private final int outputStoreIndexLeftOutEntry;
    private final int outputStoreIndexRightOutEntry;

    private final TupleList<MutableOutTuple_> dirtyCreatingList = new TupleList<>();
    private final TupleList<MutableOutTuple_> dirtyUpdatingList = new TupleList<>();
    private final TupleList<MutableOutTuple_> dirtyDyingList = new TupleList<>();

    protected AbstractJoinNode(int inputStoreIndexLeftOutTupleList, int inputStoreIndexRightOutTupleList,
            TupleLifecycle<OutTuple_> nextNodesTupleLifecycle,
            int outputStoreIndexLeftOutEntry, int outputStoreIndexRightOutEntry) {
        this.inputStoreIndexLeftOutTupleList = inputStoreIndexLeftOutTupleList;
        this.inputStoreIndexRightOutTupleList = inputStoreIndexRightOutTupleList;
        this.nextNodesTupleLifecycle = nextNodesTupleLifecycle;
        this.outputStoreIndexLeftOutEntry = outputStoreIndexLeftOutEntry;
        this.outputStoreIndexRightOutEntry = outputStoreIndexRightOutEntry;
    }

    protected abstract MutableOutTuple_ createOutTuple(LeftTuple_ leftTuple, UniTuple<Right_> rightTuple);

    protected abstract void setOutTupleLeftFacts(MutableOutTuple_ outTuple, LeftTuple_ leftTuple);

    protected abstract void setOutTupleRightFact(MutableOutTuple_ outTuple, UniTuple<Right_> rightTuple);

    protected final void insertOutTuple(LeftTuple_ leftTuple, UniTuple<Right_> rightTuple) {
        MutableOutTuple_ outTuple = createOutTuple(leftTuple, rightTuple);
        TupleList<MutableOutTuple_> outTupleListLeft = leftTuple.getStore(inputStoreIndexLeftOutTupleList);
        TupleListEntry<MutableOutTuple_> outEntryLeft = outTupleListLeft.add(outTuple);
        outTuple.setStore(outputStoreIndexLeftOutEntry, outEntryLeft);
        TupleList<MutableOutTuple_> outTupleListRight = rightTuple.getStore(inputStoreIndexRightOutTupleList);
        TupleListEntry<MutableOutTuple_> outEntryRight = outTupleListRight.add(outTuple);
        outTuple.setStore(outputStoreIndexRightOutEntry, outEntryRight);
        outTuple.setDirtyListEntry(dirtyCreatingList.add(outTuple));
    }

    protected final void doUpdateOutTuple(MutableOutTuple_ outTuple) {
        switch (outTuple.getState()) {
            case CREATING:
            case UPDATING:
                break;
            case OK:
                outTuple.setState(BavetTupleState.UPDATING);
                outTuple.setDirtyListEntry(dirtyUpdatingList.add(outTuple));
                break;
            // Impossible because they shouldn't linger in the indexes
            case DYING:
            case ABORTING:
            case DEAD:
            default:
                throw new IllegalStateException("Impossible state: The tuple (" + outTuple.getState() + ") in node (" +
                        this + ") is in an unexpected state (" + outTuple.getState() + ").");
        }
    }

    protected final void retractOutTuple(MutableOutTuple_ outTuple) {
        TupleListEntry<MutableOutTuple_> outEntryLeft = outTuple.getStore(outputStoreIndexLeftOutEntry);
        outEntryLeft.remove();
        outTuple.setStore(outputStoreIndexLeftOutEntry, null);
        TupleListEntry<MutableOutTuple_> outEntryRight = outTuple.getStore(outputStoreIndexRightOutEntry);
        outEntryRight.remove();
        outTuple.setStore(outputStoreIndexRightOutEntry, null);
        doRetractOutTuple(outTuple);
    }

    private final void doRetractOutTuple(MutableOutTuple_ outTuple) {
        switch (outTuple.getState()) {
            case CREATING:
                // Kill it before it propagates
                outTuple.getDirtyListEntry().remove();
                outTuple.setState(BavetTupleState.ABORTING);
                break;
            case UPDATING:
                outTuple.getDirtyListEntry().remove();
                outTuple.setState(BavetTupleState.DYING);
                outTuple.setDirtyListEntry(dirtyDyingList.add(outTuple));
                break;
            case OK:
                outTuple.setState(BavetTupleState.DYING);
                outTuple.setDirtyListEntry(dirtyDyingList.add(outTuple));
                break;
            // Impossible because they shouldn't linger in the indexes
            case DYING:
            case ABORTING:
            case DEAD:
            default:
                throw new IllegalStateException("Impossible state: The tuple (" + outTuple.getState() + ") in node (" +
                        this + ") is in an unexpected state (" + outTuple.getState() + ").");
        }
    }

    @Override
    public final void calculateScore() {
        dirtyCreatingList.forEachAndClear(tuple -> {
            nextNodesTupleLifecycle.insert(tuple);
            tuple.setState(BavetTupleState.OK);
        });
        dirtyUpdatingList.forEachAndClear(tuple -> {
            nextNodesTupleLifecycle.update(tuple);
            tuple.setState(BavetTupleState.OK);
        });
        dirtyDyingList.forEachAndClear(tuple -> {
            nextNodesTupleLifecycle.retract(tuple);
            tuple.setState(BavetTupleState.OK);
        });
    }

}
