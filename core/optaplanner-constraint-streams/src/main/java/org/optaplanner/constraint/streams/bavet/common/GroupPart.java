package org.optaplanner.constraint.streams.bavet.common;

public final class GroupPart<OutTuple_ extends Tuple, GroupKey_, ResultContainer_> {
    public Group<OutTuple_, GroupKey_, ResultContainer_> group;
    public Runnable undoAccumulator;

    public GroupPart(Group<OutTuple_, GroupKey_, ResultContainer_> group, Runnable undoAccumulator) {
        this.group = group;
        this.undoAccumulator = undoAccumulator;
    }
}
