package org.optaplanner.constraint.streams.common.bi;

import org.optaplanner.constraint.streams.common.AbstractTerminator;
import org.optaplanner.constraint.streams.common.ConstraintBuilder;
import org.optaplanner.constraint.streams.common.ScoreImpactType;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.bi.BiTerminator;

public final class BiTerminatorImpl<A, B>
        extends AbstractTerminator<BiTerminator<A, B>>
        implements BiTerminator<A, B> {

    public BiTerminatorImpl(ConstraintBuilder constraintBuilder, ScoreImpactType impactType, Score<?> constraintWeight) {
        super(constraintBuilder, impactType, constraintWeight);
    }

}
