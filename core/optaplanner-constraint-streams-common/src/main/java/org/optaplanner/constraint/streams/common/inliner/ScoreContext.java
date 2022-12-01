package org.optaplanner.constraint.streams.common.inliner;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.Constraint;

public abstract class ScoreContext<Score_ extends Score<Score_>> {

    private final Constraint constraint;
    private final Score_ constraintWeight;
    private final boolean constraintMatchEnabled;

    protected ScoreContext(Constraint constraint, Score_ constraintWeight, boolean constraintMatchEnabled) {
        this.constraint = constraint;
        this.constraintWeight = constraintWeight;
        this.constraintMatchEnabled = constraintMatchEnabled;
    }

    public Constraint getConstraint() {
        return constraint;
    }

    public Score_ getConstraintWeight() {
        return constraintWeight;
    }

    public boolean isConstraintMatchEnabled() {
        return constraintMatchEnabled;
    }
}
