package org.optaplanner.constraint.streams.bavet.common.tuple;

/**
 * Implements a tuple which only has a store for 1 object.
 * All store-related methods throw {@link IllegalArgumentException} for indices larger than 0.
 *
 * @implNote The benefit of this over {@link AbstractLargeStoreTuple} is that
 *           the tuple doesn't need to create and access an array,
 *           making both construction and access faster,
 *           as well as removing indirection.
 */
abstract class AbstractSingletonStoreTuple implements Tuple {

    private Object store;
    private TupleState state = TupleState.CREATING;

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
        if (index != 0) {
            throw new IllegalArgumentException("Impossible state: Singleton store tuple has no store index (" + index + ").");
        }
        return (Value_) store;
    }

    @Override
    public final void setStore(int index, Object value) {
        if (index != 0) {
            throw new IllegalArgumentException("Impossible state: Singleton store tuple has no store index (" + index + ").");
        }
        this.store = value;
    }

    @Override
    public final <Value_> Value_ removeStore(int index) {
        Value_ old = getStore(index);
        this.store = null;
        return old;
    }

}
