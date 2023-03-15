package org.optaplanner.constraint.streams.bavet.common;

import static org.optaplanner.constraint.streams.bavet.common.tuple.TupleState.DEAD;

import org.optaplanner.constraint.streams.bavet.common.tuple.Tuple;
import org.optaplanner.constraint.streams.bavet.common.tuple.TupleState;

public final class ExistsCounter<Tuple_ extends Tuple> {

    final Tuple_ leftTuple;
    TupleState state = DEAD;
    int countRight = 0;

    ExistsCounter(Tuple_ leftTuple) {
        this.leftTuple = leftTuple;
    }

    @Override
    public String toString() {
        return "Counter(" + leftTuple + ")";
    }

}
