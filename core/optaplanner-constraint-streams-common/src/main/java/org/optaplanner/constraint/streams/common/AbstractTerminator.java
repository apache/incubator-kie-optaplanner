package org.optaplanner.constraint.streams.common;

import java.util.Objects;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.Terminator;

public abstract class AbstractTerminator<Terminator_ extends Terminator<Terminator_>>
        implements Terminator<Terminator_> {
    private final ConstraintBuilder constraintBuilder;
    private final ScoreImpactType impactType;
    private final Score<?> constraintWeight;

    protected AbstractTerminator(ConstraintBuilder constraintBuilder, ScoreImpactType impactType, Score<?> constraintWeight) {
        this.constraintBuilder = Objects.requireNonNull(constraintBuilder);
        this.impactType = Objects.requireNonNull(impactType);
        this.constraintWeight = constraintWeight;
    }

    @Override
    public final Constraint as(String constraintName) {
        return constraintBuilder.apply(null, constraintName, constraintWeight, impactType);
    }

    @Override
    public final Constraint as(String constraintPackage, String constraintName) {
        return constraintBuilder.apply(constraintPackage, constraintName, constraintWeight, impactType);
    }

}
