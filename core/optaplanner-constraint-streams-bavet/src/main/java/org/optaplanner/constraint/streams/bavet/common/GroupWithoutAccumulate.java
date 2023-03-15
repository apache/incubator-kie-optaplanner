package org.optaplanner.constraint.streams.bavet.common;

import org.optaplanner.constraint.streams.bavet.common.tuple.Tuple;

final class GroupWithoutAccumulate<OutTuple_ extends Tuple, ResultContainer_>
        extends AbstractGroup<OutTuple_, ResultContainer_> {

    public GroupWithoutAccumulate(Object groupKey, OutTuple_ outTuple) {
        super(groupKey, outTuple);
    }

    @Override
    public ResultContainer_ getResultContainer() {
        throw new UnsupportedOperationException();
    }

}
