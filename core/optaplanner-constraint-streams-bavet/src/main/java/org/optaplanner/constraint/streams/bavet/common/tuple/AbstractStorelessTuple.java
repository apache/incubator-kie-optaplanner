package org.optaplanner.constraint.streams.bavet.common.tuple;

/**
 * Implements a tuple which has no store.
 * All store-related methods throw {@link UnsupportedOperationException}.
 *
 * @implNote The benefit of this over {@link AbstractLargeStoreTuple} is that the tuple doesn't need the store field,
 *           making it smaller in terms of memory footprint.
 */
abstract class AbstractStorelessTuple implements Tuple {

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
        throw new UnsupportedOperationException("Impossible state: Storeless tuple has no store index (" + index + ").");
    }

    @Override
    public final void setStore(int index, Object value) {
        throw new UnsupportedOperationException("Impossible state: Storeless tuple has no store index (" + index + ").");
    }

    @Override
    public <Value_> Value_ removeStore(int index) {
        throw new UnsupportedOperationException("Impossible state: Storeless tuple has no store index (" + index + ").");
    }

}
