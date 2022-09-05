package org.optaplanner.constraint.streams.common.tri;

import java.util.Objects;

import org.optaplanner.constraint.streams.common.ConstraintBuilder;
import org.optaplanner.constraint.streams.common.ScoreImpactType;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.tri.TriTerminator;

public final class TriTerminatorImpl<A, B, C> implements TriTerminator<A, B, C> {

    private final ConstraintBuilder constraintBuilder;
    private final ScoreImpactType impactType;
    private Score<?> constraintWeight;

    public TriTerminatorImpl(ConstraintBuilder constraintBuilder, ScoreImpactType impactType) {
        this.constraintBuilder = Objects.requireNonNull(constraintBuilder);
        this.impactType = Objects.requireNonNull(impactType);
    }

    @Override
    public TriTerminator<A, B, C> withWeight(Score<?> constraintWeight) {
        this.constraintWeight = Objects.requireNonNull(constraintWeight);
        return this;
    }

    @Override
    public Constraint as(String constraintName) {
        return constraintBuilder.apply(null, constraintName, constraintWeight, impactType);
    }

    @Override
    public Constraint as(String constraintPackage, String constraintName) {
        return constraintBuilder.apply(constraintPackage, constraintName, constraintWeight, impactType);
    }

}
