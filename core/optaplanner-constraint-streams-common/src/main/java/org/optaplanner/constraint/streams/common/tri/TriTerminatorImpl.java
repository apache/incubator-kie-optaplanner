package org.optaplanner.constraint.streams.common.tri;

import org.optaplanner.constraint.streams.common.AbstractTerminator;
import org.optaplanner.constraint.streams.common.ConstraintBuilder;
import org.optaplanner.constraint.streams.common.ScoreImpactType;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.tri.TriTerminator;

public final class TriTerminatorImpl<A, B, C>
        extends AbstractTerminator<TriTerminator<A, B, C>>
        implements TriTerminator<A, B, C> {

    public TriTerminatorImpl(ConstraintBuilder constraintBuilder, ScoreImpactType impactType, Score<?> constraintWeight) {
        super(constraintBuilder, impactType, constraintWeight);
    }

}
