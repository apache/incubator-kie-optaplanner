package org.optaplanner.constraint.streams.bavet.common;

import org.optaplanner.constraint.streams.bavet.common.collection.TupleList;
import org.optaplanner.constraint.streams.bavet.common.collection.TupleListEntry;

final class ExistsFilteringTracker<LeftTuple_ extends Tuple> {

    final ExistsCounter<LeftTuple_> counter;
    private final TupleListEntry<ExistsFilteringTracker<LeftTuple_>> leftTrackerEntry;
    private final TupleListEntry<ExistsFilteringTracker<LeftTuple_>> rightTrackerEntry;

    ExistsFilteringTracker(ExistsCounter<LeftTuple_> counter, TupleList<ExistsFilteringTracker<LeftTuple_>> leftTrackerList,
            TupleList<ExistsFilteringTracker<LeftTuple_>> rightTrackerList) {
        this.counter = counter;
        this.leftTrackerEntry = leftTrackerList.add(this);
        this.rightTrackerEntry = rightTrackerList.add(this);
    }

    public void remove() {
        leftTrackerEntry.remove();
        rightTrackerEntry.remove();
    }

}
