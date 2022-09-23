package org.optaplanner.constraint.streams.bavet.uni;

import java.util.IdentityHashMap;
import java.util.Map;

import org.optaplanner.constraint.streams.bavet.common.AbstractNode;
import org.optaplanner.constraint.streams.bavet.common.BavetTupleState;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.collection.TupleList;

public final class ForEachUniNode<A> extends AbstractNode {

    private final Class<A> forEachClass;
    /**
     * Calls for example {@link UniScorer#insert(UniTuple)}, and/or ...
     */
    private final TupleLifecycle<UniTuple<A>> nextNodesTupleLifecycle;
    private final int outputStoreSize;

    private final Map<A, UniTupleImpl<A>> tupleMap = new IdentityHashMap<>(1000);

    private final TupleList<UniTupleImpl<A>> dirtyCreatingList = new TupleList<>();
    private final TupleList<UniTupleImpl<A>> dirtyUpdatingList = new TupleList<>();
    private final TupleList<UniTupleImpl<A>> dirtyDyingList = new TupleList<>();

    public ForEachUniNode(Class<A> forEachClass, TupleLifecycle<UniTuple<A>> nextNodesTupleLifecycle, int outputStoreSize) {
        this.forEachClass = forEachClass;
        this.nextNodesTupleLifecycle = nextNodesTupleLifecycle;
        this.outputStoreSize = outputStoreSize;
    }

    public void insert(A a) {
        UniTupleImpl<A> tuple = new UniTupleImpl<>(a, outputStoreSize);
        UniTupleImpl<A> old = tupleMap.put(a, tuple);
        if (old != null) {
            throw new IllegalStateException("The fact (" + a + ") was already inserted, so it cannot insert again.");
        }
        tuple.dirtyListEntry = dirtyCreatingList.add(tuple);
    }

    public void update(A a) {
        UniTupleImpl<A> tuple = tupleMap.get(a);
        if (tuple == null) {
            throw new IllegalStateException("The fact (" + a + ") was never inserted, so it cannot update.");
        }
        switch (tuple.state) {
            case CREATING:
            case UPDATING:
                break;
            case OK:
                tuple.state = BavetTupleState.UPDATING;
                tuple.dirtyListEntry = dirtyUpdatingList.add(tuple);
                break;
            case DYING:
            case ABORTING:
            case DEAD:
            default:
                throw new IllegalStateException("Impossible state: The tuple (" + tuple + ") in node (" +
                        this + ") is in an unexpected state (" + tuple.state + ").");
        }
    }

    public void retract(A a) {
        UniTupleImpl<A> tuple = tupleMap.remove(a);
        if (tuple == null) {
            throw new IllegalStateException("The fact (" + a + ") was never inserted, so it cannot retract.");
        }
        switch (tuple.state) {
            case CREATING:
                // Kill it before it propagates
                tuple.dirtyListEntry.remove();
                tuple.state = BavetTupleState.ABORTING;
                break;
            case UPDATING:
                tuple.dirtyListEntry.remove();
                tuple.state = BavetTupleState.DYING;
                tuple.dirtyListEntry = dirtyDyingList.add(tuple);
                break;
            case OK:
                tuple.state = BavetTupleState.DYING;
                tuple.dirtyListEntry = dirtyDyingList.add(tuple);
                break;
            case DYING:
            case ABORTING:
            case DEAD:
            default:
                throw new IllegalStateException("Impossible state: The tuple (" + tuple + ") in node (" +
                        this + ") is in an unexpected state (" + tuple.state + ").");
        }
    }

    @Override
    public void calculateScore() {
        dirtyCreatingList.forEachAndClear(tuple -> {
            nextNodesTupleLifecycle.insert(tuple);
            tuple.state = BavetTupleState.OK;
        });
        dirtyUpdatingList.forEachAndClear(tuple -> {
            nextNodesTupleLifecycle.update(tuple);
            tuple.state = BavetTupleState.OK;
        });
        dirtyDyingList.forEachAndClear(tuple -> {
            nextNodesTupleLifecycle.retract(tuple);
            tuple.state = BavetTupleState.DEAD;
        });
    }

    @Override
    public String toString() {
        return super.toString() + "(" + forEachClass.getSimpleName() + ")";
    }

    public Class<A> getForEachClass() {
        return forEachClass;
    }

}
