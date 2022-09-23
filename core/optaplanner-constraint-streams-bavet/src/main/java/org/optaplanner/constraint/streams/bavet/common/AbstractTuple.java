package org.optaplanner.constraint.streams.bavet.common;

import org.optaplanner.constraint.streams.bavet.common.collection.TupleListEntry;

public abstract class AbstractTuple implements Tuple {

    /*
     * We create a lot of tuples, many of them having store size of 1.
     * If an array of size 1 was created for each such tuple, memory would be wasted and indirection created.
     * This trade-off of increased memory efficiency for marginally slower access time is proven beneficial.
     */
    private final boolean storeIsArray;

    public BavetTupleState state = BavetTupleState.CREATING;
    public TupleListEntry<? extends Tuple> dirtyListEntry; // TODO make properly generic?
    private Object store;

    protected AbstractTuple(int storeSize) {
        this.store = (storeSize < 2) ? null : new Object[storeSize];
        this.storeIsArray = store != null;
    }

    @Override
    public final BavetTupleState getState() {
        return state;
    }

    @Override
    public final void setState(BavetTupleState state) {
        this.state = state;
    }

    @Override
    public TupleListEntry<? extends Tuple> getDirtyListEntry() {
        return dirtyListEntry;
    }
    @Override
    public void setDirtyListEntry(TupleListEntry<? extends Tuple> dirtyListEntry) {
        this.dirtyListEntry = dirtyListEntry;
    }

    @Override
    public final <Value_> Value_ getStore(int index) {
        if (storeIsArray) {
            return (Value_) ((Object[]) store)[index];
        }
        return (Value_) store;
    }

    @Override
    public final void setStore(int index, Object value) {
        if (storeIsArray) {
            ((Object[]) store)[index] = value;
            return;
        }
        store = value;
    }
}
