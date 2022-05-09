package org.optaplanner.constraint.streams.bavet.common;

public final class Group<OutTuple_ extends Tuple, GroupKey_, ResultContainer_> {
    public GroupKey_ groupKey;
    public ResultContainer_ resultContainer;
    public int parentCount = 0;
    public boolean dirty = false;
    public boolean dying = false;
    public OutTuple_ tuple = null;

    public Group(GroupKey_ groupKey, ResultContainer_ resultContainer) {
        this.groupKey = groupKey;
        this.resultContainer = resultContainer;
    }
}
