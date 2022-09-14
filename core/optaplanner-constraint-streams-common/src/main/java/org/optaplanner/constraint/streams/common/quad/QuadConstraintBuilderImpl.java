package org.optaplanner.constraint.streams.common.quad;

import java.util.Collection;
import java.util.Objects;

import org.optaplanner.constraint.streams.common.AbstractConstraintBuilder;
import org.optaplanner.constraint.streams.common.ScoreImpactType;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.ConstraintJustification;
import org.optaplanner.core.api.score.stream.quad.QuadConstraintBuilder;

public final class QuadConstraintBuilderImpl<A, B, C, D>
        extends AbstractConstraintBuilder<QuadConstraintBuilder<A, B, C, D>>
        implements QuadConstraintBuilder<A, B, C, D> {

    private QuadFunction<A, B, C, D, ConstraintJustification> justificationMapping;
    private QuadFunction<A, B, C, D, Collection<?>> indictedObjectsMapping;

    public QuadConstraintBuilderImpl(QuadConstraintConstructor<A, B, C, D> constraintConstructor, ScoreImpactType impactType,
            Score<?> constraintWeight) {
        super(constraintConstructor, impactType, constraintWeight);
    }

    @Override
    protected QuadFunction<A, B, C, D, ConstraintJustification> getJustificationMapping() {
        return justificationMapping;
    }

    @Override
    public <ConstraintJustification_ extends ConstraintJustification> QuadConstraintBuilder<A, B, C, D> justifyWith(
            QuadFunction<A, B, C, D, ConstraintJustification_> justificationMapping) {
        if (this.justificationMapping != null) {
            throw new IllegalStateException("Justification mapping already set (" + justificationMapping + ").");
        }
        this.justificationMapping =
                (QuadFunction<A, B, C, D, ConstraintJustification>) Objects.requireNonNull(justificationMapping);
        return this;
    }

    @Override
    protected QuadFunction<A, B, C, D, Collection<?>> getIndictedObjectsMapping() {
        return indictedObjectsMapping;
    }

    @Override
    public QuadConstraintBuilder<A, B, C, D> indictWith(QuadFunction<A, B, C, D, Collection<?>> indictedObjectsMapping) {
        if (this.indictedObjectsMapping != null) {
            throw new IllegalStateException("Indicted objects' mapping already set (" + indictedObjectsMapping + ").");
        }
        this.indictedObjectsMapping = Objects.requireNonNull(indictedObjectsMapping);
        return this;
    }

}
