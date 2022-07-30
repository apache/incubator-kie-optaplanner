package org.optaplanner.constraint.streams.bavet.common;

public abstract class AbstractTuple implements Tuple {

    /*
     * We create a lot of tuples, many of them having store size of 1.
     * If an array of 1 was created for each such tuple, memory would be wasted.
     * This trade-off of memory efficiency for access time is proven beneficial.
     */
    private Object store;

    public BavetTupleState state = BavetTupleState.CREATING;

    protected AbstractTuple(int storeSize) {
        store = (storeSize < 2) ? null : new Object[storeSize];
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
    public final <Value_> Value_ getStore(int index) {
        if (store instanceof Object[]) {
            return (Value_) ((Object[]) store)[index];
        }
        return (Value_) store;
    }

    @Override
    public final void setStore(int index, Object value) {
        if (store instanceof Object[]) {
            ((Object[]) store)[index] = value;
            return;
        }
        store = value;
    }
}
