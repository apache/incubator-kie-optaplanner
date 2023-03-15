package org.optaplanner.constraint.streams.bavet.uni;

import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Queue;

import org.optaplanner.constraint.streams.bavet.common.AbstractNode;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.tuple.TupleState;
import org.optaplanner.constraint.streams.bavet.common.tuple.UniTuple;

public final class ForEachUniNode<A> extends AbstractNode {

    private final Class<A> forEachClass;
    /**
     * Calls for example {@link UniScorer#insert(UniTuple)}, and/or ...
     */
    private final TupleLifecycle<UniTuple<A>> nextNodesTupleLifecycle;
    private final int outputStoreSize;

    private final Map<A, UniTuple<A>> tupleMap = new IdentityHashMap<>(1000);
    private final Queue<UniTuple<A>> dirtyTupleQueue;

    public ForEachUniNode(Class<A> forEachClass, TupleLifecycle<UniTuple<A>> nextNodesTupleLifecycle, int outputStoreSize) {
        this.forEachClass = forEachClass;
        this.nextNodesTupleLifecycle = nextNodesTupleLifecycle;
        this.outputStoreSize = outputStoreSize;
        dirtyTupleQueue = new ArrayDeque<>(1000);
    }

    public void insert(A a) {
        UniTuple<A> tuple = UniTuple.of(a, outputStoreSize);
        UniTuple<A> old = tupleMap.put(a, tuple);
        if (old != null) {
            throw new IllegalStateException("The fact (" + a + ") was already inserted, so it cannot insert again.");
        }
        dirtyTupleQueue.add(tuple);
    }

    public void update(A a) {
        UniTuple<A> tuple = tupleMap.get(a);
        if (tuple == null) {
            throw new IllegalStateException("The fact (" + a + ") was never inserted, so it cannot update.");
        }
        TupleState tupleState = tuple.getState();
        if (tupleState.isDirty()) {
            if (tupleState == TupleState.DYING || tupleState == TupleState.ABORTING) {
                throw new IllegalStateException("The fact (" + a + ") was retracted, so it cannot update.");
            }
        } else {
            tuple.setState(TupleState.UPDATING);
            dirtyTupleQueue.add(tuple);
        }
    }

    public void retract(A a) {
        UniTuple<A> tuple = tupleMap.remove(a);
        if (tuple == null) {
            throw new IllegalStateException("The fact (" + a + ") was never inserted, so it cannot retract.");
        }
        TupleState tupleState = tuple.getState();
        if (tupleState.isDirty()) {
            if (tupleState == TupleState.DYING || tupleState == TupleState.ABORTING) {
                throw new IllegalStateException("The fact (" + a + ") was already retracted, so it cannot retract.");
            }
            tuple.setState(TupleState.ABORTING);
        } else {
            tuple.setState(TupleState.DYING);
            dirtyTupleQueue.add(tuple);
        }
    }

    @Override
    public void calculateScore() {
        for (UniTuple<A> tuple : dirtyTupleQueue) {
            switch (tuple.getState()) {
                case CREATING:
                    nextNodesTupleLifecycle.insert(tuple);
                    tuple.setState(TupleState.OK);
                    break;
                case UPDATING:
                    nextNodesTupleLifecycle.update(tuple);
                    tuple.setState(TupleState.OK);
                    break;
                case DYING:
                    nextNodesTupleLifecycle.retract(tuple);
                    tuple.setState(TupleState.DEAD);
                    break;
                case ABORTING:
                    tuple.setState(TupleState.DEAD);
                    break;
                case OK:
                case DEAD:
                default:
                    throw new IllegalStateException("Impossible state: The tuple (" + tuple + ") in node (" +
                            this + ") is in an unexpected state (" + tuple.getState() + ").");
            }
        }
        dirtyTupleQueue.clear();
    }

    @Override
    public String toString() {
        return super.toString() + "(" + forEachClass.getSimpleName() + ")";
    }

    public Class<A> getForEachClass() {
        return forEachClass;
    }

}
