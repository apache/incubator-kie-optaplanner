package org.optaplanner.constraint.streams.common;

import java.util.Objects;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintBuilder;

public abstract class AbstractConstraintBuilder<ConstraintBuilder_ extends ConstraintBuilder<ConstraintBuilder_>>
        implements ConstraintBuilder<ConstraintBuilder_> {
    private final org.optaplanner.constraint.streams.common.ConstraintBuilder constraintBuilder;
    private final ScoreImpactType impactType;
    private final Score<?> constraintWeight;

    protected AbstractConstraintBuilder(org.optaplanner.constraint.streams.common.ConstraintBuilder constraintBuilder,
            ScoreImpactType impactType, Score<?> constraintWeight) {
        this.constraintBuilder = Objects.requireNonNull(constraintBuilder);
        this.impactType = Objects.requireNonNull(impactType);
        this.constraintWeight = constraintWeight;
    }

    @Override
    public final Constraint asConstraint(String constraintName) {
        return constraintBuilder.apply(null, constraintName, constraintWeight, impactType);
    }

    @Override
    public final Constraint asConstraint(String constraintPackage, String constraintName) {
        return constraintBuilder.apply(constraintPackage, constraintName, constraintWeight, impactType);
    }

}
