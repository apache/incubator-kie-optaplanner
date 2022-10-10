package org.optaplanner.constraint.streams.bavet.common;

import static org.optaplanner.constraint.streams.bavet.common.BavetTupleState.DEAD;

import java.util.Queue;

public final class ExistsCounter<Tuple_ extends Tuple> {

    final Tuple_ leftTuple;
    BavetTupleState state = DEAD;
    int countRight = 0;

    ExistsCounter(Tuple_ leftTuple) {
        this.leftTuple = leftTuple;
    }

    public void insert(Queue<ExistsCounter<Tuple_>> dirtyQueue) {
        switch (state) {
            case DYING:
                state = BavetTupleState.UPDATING;
                break;
            case DEAD:
                state = BavetTupleState.CREATING;
                dirtyQueue.add(this);
                break;
            case ABORTING:
                state = BavetTupleState.CREATING;
                break;
            default:
                throw new IllegalStateException("Impossible state: the counter (" + this
                        + ") has an impossible insert state (" + state + ").");
        }
    }

    public void retract(Queue<ExistsCounter<Tuple_>> dirtyQueue) {
        switch (state) {
            case CREATING:
                // Kill it before it propagates
                state = BavetTupleState.ABORTING;
                break;
            case UPDATING:
                // Kill the original propagation
                state = BavetTupleState.DYING;
                break;
            case OK:
                state = BavetTupleState.DYING;
                dirtyQueue.add(this);
                break;
            default:
                throw new IllegalStateException("Impossible state: The counter (" + this
                        + ") has an impossible retract state (" + state + ").");
        }
    }

    @Override
    public String toString() {
        return "Counter(" + leftTuple + ")";
    }

}
