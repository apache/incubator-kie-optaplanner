package org.optaplanner.constraint.streams.bavet.common.tuple;

/**
 * @see AbstractStorelessTuple optimized for store sizes of 0.
 * @see AbstractSingletonStoreTuple optimized for store sizes of 1.
 */
abstract class AbstractLargeStoreTuple implements Tuple {

    private final Object[] store;
    private TupleState state = TupleState.CREATING;

    protected AbstractLargeStoreTuple(int storeSize) {
        if (storeSize < 2) {
            throw new IllegalArgumentException("Impossible state: unsupported tuple store size (" + storeSize + ") requested.");
        }
        this.store = new Object[storeSize];
    }

    @Override
    public final TupleState getState() {
        return state;
    }

    @Override
    public final void setState(TupleState state) {
        this.state = state;
    }

    @Override
    public final <Value_> Value_ getStore(int index) {
        if (index >= this.store.length) {
            throw new IllegalArgumentException("Impossible state: invalid tuple store index (" + index + ") requested.");
        }
        return (Value_) this.store[index];
    }

    @Override
    public final void setStore(int index, Object value) {
        if (index >= this.store.length) {
            throw new IllegalArgumentException("Impossible state: invalid tuple store index (" + index + ") requested.");
        }
        this.store[index] = value;
    }

    @Override
    public <Value_> Value_ removeStore(int index) {
        Value_ old = getStore(index);
        this.store[index] = null;
        return old;
    }

}
