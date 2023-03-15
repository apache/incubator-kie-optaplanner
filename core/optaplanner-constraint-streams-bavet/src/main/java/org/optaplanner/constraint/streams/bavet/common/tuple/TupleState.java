package org.optaplanner.constraint.streams.bavet.common.tuple;

public enum TupleState {
    CREATING(true, true),
    UPDATING(true, true),
    /**
     * Freshly refreshed tuple.
     */
    OK(false, true),
    /**
     * Tuple which was {@link #UPDATING} and then invalidated by subsequent tuple.
     */
    DYING(true, false),
    DEAD(false, false),
    /**
     * Tuple which was {@link #CREATING} and then invalidated by subsequent tuple.
     */
    ABORTING(true, false);

    private final boolean dirty;
    private final boolean active;

    TupleState(boolean dirty, boolean active) {
        this.dirty = dirty;
        this.active = active;
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean isActive() {
        return active;
    }

}
