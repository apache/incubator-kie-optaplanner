package org.optaplanner.constraint.streams.common.quad;

import java.util.Objects;

import org.optaplanner.constraint.streams.common.ConstraintBuilder;
import org.optaplanner.constraint.streams.common.ScoreImpactType;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.quad.QuadTerminator;

public final class QuadTerminatorImpl<A, B, C, D> implements QuadTerminator<A, B, C, D> {

    private final ConstraintBuilder constraintBuilder;
    private final ScoreImpactType impactType;
    private Score<?> constraintWeight;

    public QuadTerminatorImpl(ConstraintBuilder constraintBuilder, ScoreImpactType impactType) {
        this.constraintBuilder = Objects.requireNonNull(constraintBuilder);
        this.impactType = Objects.requireNonNull(impactType);
    }

    @Override
    public QuadTerminator<A, B, C, D> withWeight(Score<?> constraintWeight) {
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
