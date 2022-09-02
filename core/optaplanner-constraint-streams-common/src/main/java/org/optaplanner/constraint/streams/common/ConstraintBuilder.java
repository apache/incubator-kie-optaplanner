package org.optaplanner.constraint.streams.common;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.Constraint;

@FunctionalInterface
public interface ConstraintBuilder {

    Constraint apply(String constraintPackage, String constraintName, Score<?> constraintWeight,
            ScoreImpactType impactType);

}
