package org.optaplanner.constraint.streams.common.bi;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiFunction;

import org.optaplanner.constraint.streams.common.AbstractConstraintBuilder;
import org.optaplanner.constraint.streams.common.ScoreImpactType;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.ConstraintJustification;
import org.optaplanner.core.api.score.stream.bi.BiConstraintBuilder;

public final class BiConstraintBuilderImpl<A, B>
        extends AbstractConstraintBuilder<BiConstraintBuilder<A, B>>
        implements BiConstraintBuilder<A, B> {

    private TriFunction<A, B, Score<?>, ConstraintJustification> justificationMapping;
    private BiFunction<A, B, Collection<Object>> indictedObjectsMapping;

    public BiConstraintBuilderImpl(BiConstraintConstructor<A, B> constraintConstructor, ScoreImpactType impactType,
            Score<?> constraintWeight) {
        super(constraintConstructor, impactType, constraintWeight);
    }

    @Override
    protected TriFunction<A, B, Score<?>, ConstraintJustification> getJustificationMapping() {
        return justificationMapping;
    }

    @Override
    public <ConstraintJustification_ extends ConstraintJustification> BiConstraintBuilder<A, B> justifyWith(
            TriFunction<A, B, Score<?>, ConstraintJustification_> justificationMapping) {
        if (this.justificationMapping != null) {
            throw new IllegalStateException("Justification mapping already set (" + justificationMapping + ").");
        }
        this.justificationMapping =
                (TriFunction<A, B, Score<?>, ConstraintJustification>) Objects.requireNonNull(justificationMapping);
        return this;
    }

    @Override
    protected BiFunction<A, B, Collection<Object>> getIndictedObjectsMapping() {
        return indictedObjectsMapping;
    }

    @Override
    public BiConstraintBuilder<A, B> indictWith(BiFunction<A, B, Collection<Object>> indictedObjectsMapping) {
        if (this.indictedObjectsMapping != null) {
            throw new IllegalStateException("Indicted objects' mapping already set (" + indictedObjectsMapping + ").");
        }
        this.indictedObjectsMapping = Objects.requireNonNull(indictedObjectsMapping);
        return this;
    }

}
