package org.optaplanner.core.api.score.stream.quad;

import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintBuilder;
import org.optaplanner.core.api.score.stream.ConstraintJustification;

/**
 * Used to build a {@link Constraint} out of a {@link QuadConstraintStream}, applying optional configuration.
 * To build the constraint, use one of the terminal operations, such as {@link #asConstraint(String)}.
 * <p>
 * Unless {@link #justifyWith(QuadFunction)} is called, the default justification mapping will be used.
 */
public interface QuadConstraintBuilder<A, B, C, D> extends ConstraintBuilder<QuadConstraintBuilder<A, B, C, D>> {

    /**
     * Sets a custom function to apply on a constraint match to justify it.
     *
     * @see ConstraintMatch
     * @param justificationMapping never null
     * @return this
     */
    <ConstraintJustification_ extends ConstraintJustification> QuadConstraintBuilder<A, B, C, D> justifyWith(
            QuadFunction<A, B, C, D, ConstraintJustification_> justificationMapping);

}
